package evm.main.comment.dto;

import evm.main.event.dto.EventShortDto;
import evm.main.users.dto.UserShortDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCommentDto {

    @Size(min = 1, max = 4000)
    @NotBlank
    private String text;

    @NotNull
    private LocalDateTime publishedDate;

    @NotNull
    private UserShortDto author;

    @NotNull
    private EventShortDto event;

}
