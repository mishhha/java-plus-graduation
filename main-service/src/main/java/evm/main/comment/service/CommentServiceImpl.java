package evm.main.comment.service;

import evm.main.category.model.Category;
import evm.main.comment.dto.CommentDto;
import evm.main.comment.dto.NewCommentDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserRepositoryJpa userRepository;
    private final EventRepository eventRepository;

    @Override
    CommentDto createComment(NewCommentDto dto) {
      //
    };

    @Override
    void deleteCommentById(Long commentId) {
        //
    };

    @Override
    void deleteAllCommentsByAuthor(Long authorId) {
        //
    };

    @Override
    void deleteAllCommentByEvent(Long eventId) {
        //
    };

    @Override
    CommentDto updateComment(Long commentId, NewCommentDto dto) {
        //
    };

    @Override
    CommentDto getComment(Long commentId) {
        //
    };

    @Override
    List<CommentDto> getAllCommentsByEvent(Long eventId, Integer from, Integer size) {
        //
    };

    @Override
    List<CommentDto> getAllCommentsByAuthor(Long authorId, Integer from, Integer size) {

    };

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
