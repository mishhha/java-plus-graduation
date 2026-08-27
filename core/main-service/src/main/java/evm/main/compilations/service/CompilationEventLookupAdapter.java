package evm.main.compilations.service;

import evm.compilation.port.EventLookupPort;
import evm.event.dto.EventShortDto;
import evm.event.mapper.EventMapper;
import evm.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompilationEventLookupAdapter implements EventLookupPort {

    private final EventRepository eventRepository;

    @Override
    public List<EventShortDto> findByIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }
        return eventRepository.findAllById(eventIds).stream()
            .map(e -> EventMapper.toShortDto(e, 0L, 0L))
            .collect(Collectors.toList());
    }
}