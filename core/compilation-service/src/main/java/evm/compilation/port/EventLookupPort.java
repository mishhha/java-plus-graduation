package evm.compilation.port;

import java.util.List;

/**
 * Порт для получения событий по их ID.
 * Реализация находится в event-service.
 */
public interface EventLookupPort {

    List<Object> findByIds(List<Long> eventIds);
}