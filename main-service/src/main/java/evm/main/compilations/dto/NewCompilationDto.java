package evm.main.compilations.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class NewCompilationDto {
    private List<Long> events;

    @JsonProperty(defaultValue = "false")
    private Boolean pinned;

    @NotBlank
    @Size(min = 1, max = 50)
    private String title;
}
