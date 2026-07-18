package evm.main.comment.mappper;

import evm.main.comment.dto.CommentDto;
import evm.main.comment.dto.NewCommentDto;
import evm.main.comment.model.Comment;
import evm.main.event.dto.EventShortDto;
import evm.main.users.dto.UserShortDto;

public class CommentMapper {

    private CommentMapper() {
    }

    public static Comment toEntity(NewCommentDto dto, UserShortDto userShortDto, EventShortDto eventShortDto) {
        return Comment.builder()
                .text(dto.getText())
                .publishedDate(dto.getPublishedDate())
                .author(userShortDto)
                .event(eventShortDto)
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
