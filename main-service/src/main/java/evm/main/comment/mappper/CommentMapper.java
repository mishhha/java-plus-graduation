package evm.main.comment.mappper;

import evm.main.comment.dto.CommentDto;
import evm.main.comment.dto.NewCommentDto;
import evm.main.comment.model.Comment;
import evm.main.event.model.Event;
import evm.main.users.model.User;

public class CommentMapper {

    private CommentMapper() {
    }

    public static Comment toEntity(NewCommentDto dto, User user, Event event) {
        return Comment.builder()
                .text(dto.getText())
                .publishedDate(dto.getPublishedDate())
                .author(user)
                .event(event)
                .build();
    }

    public static CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(comment.getAuthor())
                .event(comment.getEvent())
                .build();
    }
}
