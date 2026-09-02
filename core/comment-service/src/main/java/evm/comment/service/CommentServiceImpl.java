package evm.comment.service;

import evm.comment.client.dto.EventClient;
import evm.comment.client.dto.EventShortDto;
import evm.comment.dto.CommentDto;
import evm.comment.dto.NewCommentDto;
import evm.comment.mapper.CommentMapper;
import evm.comment.model.Comment;
import evm.comment.repository.CommentRepository;
import evm.common.exceptions.ConflictException;
import evm.common.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import feign.FeignException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper mapper;
    private final EventClient eventClient;

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllCommentsFiltered(Long eventId, Long authorId, Integer from, Integer size) {
        if (eventId != null && authorId != null) {
            throw new IllegalArgumentException("Можно указать только один параметр: eventId или authorId");
        }
        if (eventId != null) return getAllCommentsByEvent(eventId, from, size);
        if (authorId != null) return getAllCommentsByAuthor(authorId, from, size);
        throw new IllegalArgumentException("Необходимо указать параметр eventId или authorId");
    }

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto dto) {
        EventShortDto event;
        try {
            // Пытаемся получить событие через Feign
            event = eventClient.getEvent(eventId);
        } catch (FeignException e) {
            // Если event-service вернул 404, 500 или недоступен, считаем, что события нет
            throw new NotFoundException("Событие с id=" + eventId + " не найдено или сервис событий недоступен");
        }

        // Теперь проверяем статус (Feign вернул 200 OK, но событие может быть в статусе PENDING)
        if (!"PUBLISHED".equals(event.getState())) {
            throw new ConflictException("Нельзя комментировать неопубликованное событие");
        }

        Comment c = mapper.toEntity(dto, userId, eventId);
        Comment saved = commentRepository.save(c);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = findCommentOrRaiseException(commentId);
        if (!comment.getAuthorId().equals(userId)) {
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
        commentRepository.deleteAllByAuthorId(authorId);
    }

    @Override
    @Transactional
    public void deleteAllCommentsByEvent(Long eventId) {
        commentRepository.deleteAllByEventId(eventId);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto dto) {
        Comment c = findCommentOrRaiseException(commentId);
        if (!c.getAuthorId().equals(userId)) {
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
        int fromVal = (from != null) ? from : 0;
        int sizeVal = (size != null) ? size : 10;
        Pageable p = PageRequest.of(fromVal / sizeVal, sizeVal);
        // 👈 Благодаря @Cacheable в маппере, N+1 не возникнет!
        return commentRepository.findAllByEventIdOrderByCreatedDesc(eventId, p)
            .stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllCommentsByAuthor(Long authorId, Integer from, Integer size) {
        int fromVal = (from != null) ? from : 0;
        int sizeVal = (size != null) ? size : 10;
        Pageable p = PageRequest.of(fromVal / sizeVal, sizeVal);
        return commentRepository.findAllByAuthorIdOrderByCreatedDesc(authorId, p)
            .stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAllCommentsFiltered(Long eventId, Long authorId) {
        if (eventId != null && authorId != null) {
            throw new IllegalArgumentException("Можно указать только один параметр: eventId или authorId");
        }
        if (eventId != null) { deleteAllCommentsByEvent(eventId); return; }
        if (authorId != null) { deleteAllCommentsByAuthor(authorId); return; }
        throw new IllegalArgumentException("Необходимо указать параметр eventId или authorId");
    }

    private Comment findCommentOrRaiseException(Long commentId) {
        return commentRepository.findById(commentId)
            .orElseThrow(() -> new NotFoundException("Комментарий с id = " + commentId + " не найден!"));
    }
}