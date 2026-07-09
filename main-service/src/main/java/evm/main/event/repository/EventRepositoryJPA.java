package evm.main.event.repository;

import evm.main.event.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepositoryJPA extends JpaRepository<Event, Long> {
}
