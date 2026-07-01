package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ViewStatsDto {
    @NotBlank
    private String app;

    @NotBlank
    private String uri;

    @NotNull
    private Long hits;
}
