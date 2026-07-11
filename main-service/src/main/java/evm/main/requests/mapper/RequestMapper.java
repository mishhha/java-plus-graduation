package evm.main.requests.mapper;

import evm.main.event.model.Event;
import evm.main.requests.dto.ResponseRequestDto;
import evm.main.requests.model.Request;
import evm.main.requests.model.Status;
import evm.main.users.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RequestMapper {

    public Request mapToRequest(User user, Event event) {

        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setStatus(Status.PENDING);
        request.setRequester(user);
        request.setEvent(event);

        return request;

    }

    public ResponseRequestDto mapToResponseRequestDto(Request request) {

        ResponseRequestDto dto = new ResponseRequestDto();
        dto.setRequester(request.getRequester().getId());
        dto.setId(request.getId());
        dto.setStatus(request.getStatus());
        dto.setEvent(request.getEvent().getId());
        dto.setCreated(request.getCreated());

        return dto;

    }

}
