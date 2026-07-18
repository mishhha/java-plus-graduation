package evm.main.comment.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateCommentDto {
    private Long Id;
    private String text;
}
