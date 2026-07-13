package evm.main.requests.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
//import evm.main.requests.model.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseRequestDto {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime created;
    Long event;
    Long id;
    Long requester;
    String status;

}
