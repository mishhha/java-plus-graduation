package evm.main.requests.repository;

import evm.main.requests.model.Request;
import evm.main.requests.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RequestRepositoryJpa extends JpaRepository<Request, Long> {

    @Query("select r from Request as r where r.requester.id = ?1")
    List<Request> findAllByUserId(Long userId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    @Query("select COUNT(r) from Request AS r where r.event.id = ?1 AND r.status = ?2")
    long countByEventIdAndStatus(Long eventId, Status status);

    Optional<Request> findByEventIdAndRequesterId(Long eventId, Long requesterId);
}