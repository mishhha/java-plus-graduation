package evm.main.comment.service;

import evm.main.comment.dto.CommentDto;
import evm.main.comment.dto.NewCommentDto;
import evm.main.comment.mappper.CommentMapper;
import evm.main.comment.repository.CommentRepository;
import evm.main.event.repository.EventRepository;
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
    void deleteComment(Long commentId) {
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

}
