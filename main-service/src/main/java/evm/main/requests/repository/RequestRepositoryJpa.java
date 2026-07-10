package evm.main.requests.repository;

import evm.main.requests.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RequestRepositoryJpa extends JpaRepository<Request, Long> {

    @Query("select r from Request as r where r.requester.id = ?1")
    List<Request> findAllByUserId(Long userId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);
    
}