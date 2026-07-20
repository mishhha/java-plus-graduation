package evm.main.comment.mappper;

import evm.main.comment.dto.CommentDto;
import evm.main.comment.model.Comment;
import evm.main.event.model.Event;
import evm.main.users.model.User;

import java.time.LocalDateTime;

public class CommentMapper {

    private CommentMapper() {
    }

    public static Comment toEntity(String text, User author, Event event) {
        return Comment.builder()
                .text(text)
                .event(event)
                .author(author)
                .publishedDate(LocalDateTime.now())
                .build();
    }

    public static CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .eventId(comment.getEvent().getId())
                .authorId(comment.getAuthor().getId())
                .publishedDate(comment.getPublishedDate())
                .editedOn(comment.getEditedOn())
                .build();
    }
}
