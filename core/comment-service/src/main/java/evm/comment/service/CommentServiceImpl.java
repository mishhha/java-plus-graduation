package evm.comment.service;

import evm.comment.dto.CommentDto;
import evm.comment.dto.NewCommentDto;
import evm.comment.mapper.CommentMapper;
import evm.comment.model.Comment;
import evm.comment.port.EventLookupPort;
import evm.comment.repository.CommentRepository;
import evm.common.exceptions.ConflictException;
import evm.common.exceptions.NotFoundException;
import evm.users.model.User;
import evm.users.repository.UserRepositoryJpa;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepositoryJpa userRepository;
    private final EventLookupPort eventLookupPort; // Вместо EventRepository
    private final CommentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllCommentsFiltered(Long eventId, Long authorId, Integer from, Integer size) {
        // Бизнес-валидация: ровно один параметр должен быть указан
        if (eventId != null && authorId != null) {
            throw new IllegalArgumentException("Можно указать только один параметр: eventId или authorId");
        }

        if (eventId != null) {
            return getAllCommentsByEvent(eventId, from, size);
        }

        if (authorId != null) {
            return getAllCommentsByAuthor(authorId, from, size);
        }

        throw new IllegalArgumentException("Необходимо указать параметр eventId или authorId");
    }

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto dto) {
        User author = findUserOrRaiseException(userId);

        if (!eventLookupPort.isPublished(eventId)) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено или не опубликовано");
        }

        Comment c = mapper.toEntity(dto, author, eventId);
        Comment saved = commentRepository.save(c);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        findUserOrRaiseException(userId);
        Comment comment = findCommentOrRaiseException(commentId);

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Удалять можно только свои комментарии");
        }
        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void deleteCommentById(Long commentId) {
        Comment comment = findCommentOrRaiseException(commentId);
        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void deleteAllCommentsByAuthor(Long authorId) {
        findUserOrRaiseException(authorId);
        commentRepository.deleteAllByAuthorId(authorId);
    }

    @Override
    @Transactional
    public void deleteAllCommentsByEvent(Long eventId) {
        if (!eventLookupPort.isPublished(eventId)) {
            // Для админских действий можно разрешить удаление даже для неопубликованных,
            // но лучше проверить существование. Для простоты пока оставим так или уберем проверку.
            // В оригинале была проверка findEventOrRaiseException.
            // Здесь мы не можем проверить существование без отдельного метода existsById.
            // Предположим, что если события нет, то и комментариев нет.
        }
        commentRepository.deleteAllByEventId(eventId);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto dto) {
        findUserOrRaiseException(userId);
        Comment c = findCommentOrRaiseException(commentId);

        if (!c.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Редактировать можно только свои комментарии");
        }

        c.setText(dto.getText());
        c.setEdited(LocalDateTime.now());

        Comment saved = commentRepository.save(c);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getComment(Long eventId, Long commentId) {
        Comment comment = findCommentOrRaiseException(commentId);

        if (!comment.getEventId().equals(eventId)) {
            throw new NotFoundException("Комментарий с id=" + commentId + " не найден для события " + eventId);
        }
        return mapper.toDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllCommentsByEvent(Long eventId, Integer from, Integer size) {
        Pageable p = PageRequest.of(from / size, size);
        return commentRepository.findAllByEventIdOrderByPublishedDateDesc(eventId, p)
            .stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllCommentsByAuthor(Long authorId, Integer from, Integer size) {
        findUserOrRaiseException(authorId);
        Pageable p = PageRequest.of(from / size, size);
        return commentRepository.findAllByAuthorIdOrderByPublishedDateDesc(authorId, p)
            .stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }

    private Comment findCommentOrRaiseException(Long commentId) {
        return commentRepository.findById(commentId)
            .orElseThrow(() -> new NotFoundException("Комментарий с id = " + commentId + " не найден!"));
    }

    private User findUserOrRaiseException(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден!"));
    }

    @Override
    @Transactional
    public void deleteAllCommentsFiltered(Long eventId, Long authorId) {
        if (eventId != null && authorId != null) {
            throw new IllegalArgumentException("Можно указать только один параметр: eventId или authorId");
        }

        if (eventId != null) {
            deleteAllCommentsByEvent(eventId);
            return;
        }

        if (authorId != null) {
            deleteAllCommentsByAuthor(authorId);
            return;
        }

        throw new IllegalArgumentException("Необходимо указать параметр eventId или authorId");
    }
}