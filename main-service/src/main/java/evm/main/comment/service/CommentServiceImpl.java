package evm.main.comment.service;

import evm.main.comment.dto.CommentDto;
import evm.main.comment.dto.NewCommentDto;
import evm.main.comment.dto.UpdateCommentDto;
import evm.main.comment.mappper.CommentMapper;
import evm.main.comment.model.Comment;
import evm.main.comment.repository.CommentRepository;
import evm.main.event.model.Event;
import evm.main.event.repository.EventRepository;
import evm.main.exceptions.NotFoundException;
import evm.main.users.model.User;
import evm.main.users.repository.UserRepositoryJpa;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepositoryJpa userRepository;
    private final EventRepository eventRepository;

    @Override
    public CommentDto createComment(NewCommentDto dto) {
        Event event = findEventOrRaiseException(dto.getEvent().getId());
        User author = findUserOrRaiseException(dto.getAuthor().getId());
        Comment c = CommentMapper.toEntity(dto, dto.getAuthor(), dto.getEvent());
        Comment saved = commentRepository.save(c);
        return CommentMapper.toDto(saved);
    }

    @Override
    public void deleteCommentById(Long commentId) {
        commentRepository.deleteById(findCommentOrRaiseException(commentId).getId());
    }

    @Override
    public void deleteAllCommentsByAuthor(Long authorId) {
        User author = findUserOrRaiseException(authorId);
        commentRepository.deleteAllCommentsByAuthorId(author.getId());
    }

    @Override
    public void deleteAllCommentByEvent(Long eventId) {
        Event e = findEventOrRaiseException(eventId);
        commentRepository.deleteAllCommentsByEventId(e.getId());
    }

    @Override
    public CommentDto updateComment(UpdateCommentDto dto) {
        Comment c = findCommentOrRaiseException(dto.getId());
        c.setText(dto.getText());
        Comment saved = commentRepository.save(c);
        return CommentMapper.toDto(saved);
    }

    @Override
    public CommentDto getComment(Long commentId) {
        Comment comment = findCommentOrRaiseException(commentId);
        return CommentMapper.toDto(comment);
    }

    @Override
    public List<CommentDto> getAllCommentsByEvent(Long eventId, Integer from, Integer size) {
        Event e = findEventOrRaiseException(eventId);
        Pageable p = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "published_date"));
        return commentRepository.findAllCommentsByEventId(e.getId(), p).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getAllCommentsByAuthor(Long authorId, Integer from, Integer size) {
        User author = findUserOrRaiseException(authorId);
        Pageable p = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "published_date"));
        return commentRepository.findAllCommentsByAuthorId(author.getId(), p).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    private Comment findCommentOrRaiseException(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id = " + commentId.toString() + " не найден!"));
    }

    private Event findEventOrRaiseException(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId.toString() + " не найдено!"));
    }

    private User findUserOrRaiseException(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Автор/пользователь с id = " + userId.toString() + " не найден!"));
    }

}
