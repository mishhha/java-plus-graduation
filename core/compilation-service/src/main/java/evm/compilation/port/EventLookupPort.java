package evm.compilation.port;

import evm.event.dto.EventShortDto;

import java.util.List;

/**
 * Порт получения событий по ID.
 * Реализация находится в main-service (адаптер над EventRepository).
 */
public interface EventLookupPort {

    List<EventShortDto> findByIds(List<Long> eventIds);
}