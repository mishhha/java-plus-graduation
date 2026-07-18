package evm.main.comment.controller;

import evm.main.comment.dto.CommentDto;
import evm.main.comment.dto.NewCommentDto;
import evm.main.comment.dto.UpdateCommentDto;
import evm.main.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class PrivateCommentController {

    private final CommentService commentService;

    // то, что можно только автору комментария

    @PostMapping("/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@Valid @RequestBody NewCommentDto newCommentDto) {
        return commentService.createComment(newCommentDto);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        commentService.deleteCommentById(commentId);
    }

    @PatchMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@Valid @RequestBody UpdateCommentDto updateCommentDto) {
        return commentService.updateComment(updateCommentDto);
    }

}
