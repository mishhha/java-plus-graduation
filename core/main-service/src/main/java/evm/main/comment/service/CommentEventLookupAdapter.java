package evm.main.comment.service;

import evm.comment.port.EventLookupPort;
import evm.event.model.EventState;
import evm.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentEventLookupAdapter implements EventLookupPort {

    private final EventRepository eventRepository;

    @Override
    public boolean isPublished(Long eventId) {
        return eventRepository.findByIdAndState(eventId, EventState.PUBLISHED).isPresent();
    }

}