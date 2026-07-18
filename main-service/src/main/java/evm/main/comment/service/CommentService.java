package evm.main.comment.service;

import evm.main.comment.dto.CommentDto;
import evm.main.comment.dto.NewCommentDto;
import evm.main.comment.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(NewCommentDto dto);

    void deleteCommentById(Long commentId);

    void deleteAllCommentsByAuthor(Long authorId);

    void deleteAllCommentsByEvent(Long eventId);

    CommentDto updateComment(UpdateCommentDto dto);

    CommentDto getCommentById(Long commentId);

    List<CommentDto> getAllCommentsByEvent(Long eventId, Integer from, Integer size);

    List<CommentDto> getAllCommentsByAuthor(Long authorId, Integer from, Integer size);

}