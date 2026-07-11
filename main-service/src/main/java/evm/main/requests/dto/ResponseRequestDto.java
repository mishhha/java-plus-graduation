package evm.main.requests.dto;

import evm.main.requests.model.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseRequestDto {

    LocalDateTime created;
    Long event;
    Long id;
    Long requester;
    Status status;

}
