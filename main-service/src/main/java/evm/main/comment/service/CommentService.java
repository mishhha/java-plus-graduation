package evm.main.comment.service;

import evm.main.comment.dto.CommentDto;
import evm.main.comment.dto.NewCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(NewCommentDto dto);

    void deleteCommentById(Long commentId);

    void deleteAllCommentsByAuthor(Long authorId);

    void deleteAllCommentByEvent(Long eventId);

    CommentDto updateComment(Long commentId, NewCommentDto dto);

    CommentDto getComment(Long commentId);

    List<CommentDto> getAllCommentsByEvent(Long eventId, Integer from, Integer size);

    List<CommentDto> getAllCommentsByAuthor(Long authorId, Integer from, Integer size);

}