package evm.comment.mapper;

import evm.comment.dto.CommentDto;
import evm.comment.dto.NewCommentDto;
import evm.comment.model.Comment;
import evm.users.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CommentMapper {

    public CommentDto toDto(Comment comment) {
        return CommentDto.builder()
            .id(comment.getId())
            .text(comment.getText())
            .authorId(comment.getAuthor().getId())                .authorName(comment.getAuthor().getName())
            .eventId(comment.getEventId()) // Было: event.getId()
            .publishedDate(comment.getCreated())
            .editedOn(comment.getEdited())
            .build();
    }

    public Comment toEntity(NewCommentDto dto, User author, Long eventId) {
        return Comment.builder()
            .text(dto.getText())
            .author(author)
            .eventId(eventId) // Было: event
            .created(LocalDateTime.now())
            .build();
    }
}