package evm.comment.mapper;

import evm.comment.client.UserClient;
import evm.comment.client.dto.UserDto;
import evm.comment.dto.CommentDto;
import evm.comment.dto.NewCommentDto;
import evm.comment.model.Comment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 👈 Убедитесь, что это есть
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentMapper {

    private final UserClient userClient;

    public CommentDto toDto(Comment comment) {
        String authorName = "Unknown";
        try {
            log.info("Запрос к user-service за пользователем с ID: {}", comment.getAuthorId());
            List<UserDto> users = userClient.getUsersByIds(List.of(comment.getAuthorId()));

            if (users != null && !users.isEmpty()) {
                authorName = users.get(0).getName();
                log.info("Успешно получено имя: {}", authorName);
            } else {
                log.warn("user-service вернул пустой список для ID: {}", comment.getAuthorId());
            }
        } catch (Exception e) {
            // 👈 ВОТ ЗДЕСЬ МЫ УВИДИМ ИСТИННУЮ ПРИЧИНУ!
            log.error("Ошибка при получении имени пользователя через Feign для ID {}: {}", comment.getAuthorId(), e.getMessage());
        }

        return CommentDto.builder()
            .id(comment.getId())
            .text(comment.getText())
            .authorId(comment.getAuthorId())
            .authorName(authorName)
            .eventId(comment.getEventId())
            .publishedDate(comment.getCreated())
            .editedOn(comment.getEdited())
            .build();
    }

    public Comment toEntity(NewCommentDto dto, Long authorId, Long eventId) {
        return Comment.builder()
            .text(dto.getText())
            .authorId(authorId)
            .eventId(eventId)
            .created(LocalDateTime.now())
            .build();
    }
}