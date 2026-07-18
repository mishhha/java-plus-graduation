package evm.main.comment.controller;

import evm.main.comment.dto.CommentDto;
import evm.main.comment.service.CommentService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor

public class PublicCommentController {

    public final CommentService commentService;

    // то, что можно всем пользователям

    @GetMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto getCommentById(@PathVariable Long commentId) {
        return commentService.getCommentById(commentId);
    }

    @GetMapping("/comments/byEvent/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getAllCommentsByEvent(
            @PathVariable Long eventId,
            @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(value = "size", defaultValue = "10") @Positive Integer size) {
        return commentService.getAllCommentsByEvent(eventId, from, size);
    }



}
