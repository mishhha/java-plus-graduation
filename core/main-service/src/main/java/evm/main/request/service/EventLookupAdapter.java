package evm.main.request.service;

import evm.event.model.Event;
import evm.event.repository.EventRepository;
import evm.request.dto.EventInfo;
import evm.request.port.EventLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventLookupAdapter implements EventLookupPort {

    private final EventRepository eventRepository;

    public EventInfo findById(Long eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return null;

        EventInfo info = new EventInfo();
        info.setId(event.getId());
        info.setInitiatorId(event.getInitiator().getId());
        info.setState(event.getState().toString());
        info.setParticipantLimit(event.getParticipantLimit());
        info.setRequestModeration(event.getRequestModeration());
        info.setPublishedOn(event.getPublishedOn());   // ← ДОБАВИТЬ
        return info;
    }
}