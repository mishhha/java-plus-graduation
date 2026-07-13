package evm.main.event.repository;

import evm.main.event.model.Event;
import evm.main.event.model.EventState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    // События конкретного пользователя с пагинацией
    List<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    // Получить события по списку id — нужно для подборок (Compilation)
    List<Event> findAllByIdIn(List<Long> ids);

    // Опубликованное событие по id — для публичного API
    Optional<Event> findByIdAndState(Long id, EventState state);

    // Проверка — есть ли события в категории (перед удалением категории)
    boolean existsByCategoryId(Long categoryId);

    // Публичный поиск событий с фильтрацией
    @Query(value = """
            SELECT * FROM events e
            WHERE e.state = 'PUBLISHED'
            AND (:text IS NULL OR
                LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR
                LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')))
            AND (:categories IS NULL OR e.category_id IN (:categories))
            AND (:paid IS NULL OR e.paid = :paid)
            AND (e.event_date > CAST(:rangeStart AS timestamp))
            AND (CAST(:rangeEnd AS timestamp) IS NULL
                 OR e.event_date < CAST(:rangeEnd AS timestamp))
            ORDER BY e.event_date
            """,
            countQuery = """
                    SELECT COUNT(*) FROM events e
                    WHERE e.state = 'PUBLISHED'
                    AND (:text IS NULL OR
                        LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR
                        LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')))
                    AND (:categories IS NULL OR e.category_id IN (:categories))
                    AND (:paid IS NULL OR e.paid = :paid)
                    AND (e.event_date > CAST(:rangeStart AS timestamp))
                    AND (CAST(:rangeEnd AS timestamp) IS NULL
                         OR e.event_date < CAST(:rangeEnd AS timestamp))
                    """,
            nativeQuery = true)
    List<Event> findPublicEvents(
            @Param("text") String text,
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable
    );

    // Административный поиск событий с фильтрацией по статусам и пользователям
    @Query(value = """
            SELECT * FROM events e
            WHERE (:users IS NULL OR e.initiator_id IN (:users))
            AND (:states IS NULL OR e.state IN (:states))
            AND (:categories IS NULL OR e.category_id IN (:categories))
            AND (CAST(:rangeStart AS timestamp) IS NULL
                 OR e.event_date >= CAST(:rangeStart AS timestamp))
            AND (CAST(:rangeEnd AS timestamp) IS NULL
                 OR e.event_date <= CAST(:rangeEnd AS timestamp))
            ORDER BY e.id
            """,
            countQuery = """
                    SELECT COUNT(*) FROM events e
                    WHERE (:users IS NULL OR e.initiator_id IN (:users))
                    AND (:states IS NULL OR e.state IN (:states))
                    AND (:categories IS NULL OR e.category_id IN (:categories))
                    AND (CAST(:rangeStart AS timestamp) IS NULL
                         OR e.event_date >= CAST(:rangeStart AS timestamp))
                    AND (CAST(:rangeEnd AS timestamp) IS NULL
                         OR e.event_date <= CAST(:rangeEnd AS timestamp))
                    """,
            nativeQuery = true)
    List<Event> findAdminEvents(
            @Param("users") List<Long> users,
            @Param("states") List<String> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable
    );
}
