package evm.main.request.service;

import evm.event.port.RequestStatsPort;
import evm.request.model.Status;
import evm.request.repository.RequestRepositoryJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RequestStatsAdapter implements RequestStatsPort {

    private final RequestRepositoryJpa requestRepository;

    @Override
    public long countConfirmed(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
    }

    @Override
    public Map<Long, Long> countConfirmedByEventIds(List<Long> eventIds) {
        if (eventIds.isEmpty()) return Map.of();
        return requestRepository.countConfirmedByEventIds(eventIds).stream()
            .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
    }
}