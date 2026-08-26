package evm.request.mapper;

import evm.request.dto.ParticipationRequestDto;
import evm.request.model.Request;
import evm.request.model.Status;
import evm.users.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RequestMapper {

    public ParticipationRequestDto toDto(Request request) {
        return ParticipationRequestDto.builder()
            .id(request.getId())
            .requester(request.getRequester().getId())
            .event(request.getEventId())
            .status(request.getStatus().name())
            .created(request.getCreated())
            .build();
    }

    public Request mapToRequest(User user, Long eventId) {
        Request request = new Request();
        request.setRequester(user);
        request.setEventId(eventId);
        request.setStatus(Status.PENDING);
        request.setCreated(LocalDateTime.now());
        return request;
    }
}