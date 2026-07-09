package evm.main.requests.dto;

import evm.main.requests.model.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseRequestDto {

    Long id;
    LocalDateTime created;
    Long event;
    Long requester;
    Status status;

}
