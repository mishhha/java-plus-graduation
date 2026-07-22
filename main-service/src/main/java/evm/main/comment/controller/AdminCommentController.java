package evm.main.comment.controller;

import evm.main.comment.dto.CommentDto;
import evm.main.comment.service.CommentService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/comments")
@RequiredArgsConstructor
@Validated
public class AdminCommentController {

    private final CommentService commentService;

    // то, что можно только администратору

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getComments(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        if (eventId != null && authorId != null) {
            throw new IllegalArgumentException(
                    "Можно указать только один параметр: eventId или authorId");
        }

        if (eventId != null) {
            return commentService.getAllCommentsByEvent(eventId, from, size);
        }

        if (authorId != null) {
            return commentService.getAllCommentsByAuthor(authorId, from, size);
        }

        throw new IllegalArgumentException(
                "Необходимо указать параметр eventId или authorId");
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        commentService.deleteCommentById(commentId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComments(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long authorId) {

        if (eventId != null && authorId != null) {
            throw new IllegalArgumentException(
                    "Можно указать только один параметр: eventId или authorId");
        }

        if (eventId != null) {
            commentService.deleteAllCommentsByEvent(eventId);
            return;
        }

        if (authorId != null) {
            commentService.deleteAllCommentsByAuthor(authorId);
            return;
        }

        throw new IllegalArgumentException(
                "Необходимо указать параметр eventId или authorId");
    }

}
