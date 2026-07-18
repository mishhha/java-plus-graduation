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
@RequestMapping(path = "/admin/comments")
public class AdminCommentController {

    private final CommentService commentService;

    // то, что можно только администратору

    @GetMapping("/byAuthor/{authorId}")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getAllCommentsByAuthor(
            @PathVariable Long authorId,
            @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(value = "size", defaultValue = "10") @Positive Integer size) {
        return commentService.getAllCommentsByAuthor(authorId, from, size);
    }

    @DeleteMapping("/byAuthor/{authorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllCommentsByAuthor(@PathVariable Long authortId) {
        commentService.deleteAllCommentsByAuthor(authortId);
    }

    @DeleteMapping("/byEvent/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllCommentsByEvent(@PathVariable Long eventId) {
        commentService.deleteAllCommentsByEvent(eventId);
    }

}
