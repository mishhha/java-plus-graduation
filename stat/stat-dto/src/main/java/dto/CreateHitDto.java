package dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateHitDto {
    @NotBlank(message = "app не должен быть пустым")
    @Size(max = 512, message = "допустимая длина app - не более 512 символов")
    private String app;

    @NotBlank(message = "uri не должен быть пустым")
    @Size(max = 1024, message = "допустимая длина uri - не более 1024 символов")
    private String uri;

    @NotBlank(message = "ip не должен быть пустым")
    @Pattern(
            regexp = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$",
            message = "некорректный формат ip"
    )
    private String ip;

    @NotBlank(message = "timestamp не должен быть пустым")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String timestamp;
}
