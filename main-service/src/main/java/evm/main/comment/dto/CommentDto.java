package evm.main.comment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import evm.main.event.model.Event;
import evm.main.users.model.User;
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
public class CommentDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Size(min = 1, max = 4000)
    @NotBlank
    private String text;

    @NotNull
    private LocalDateTime publishedDate;

    @NotNull
    private User author;

    @NotNull
    private Event event;

}

