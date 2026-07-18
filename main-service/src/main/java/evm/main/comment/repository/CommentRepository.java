package evm.main.comment.repository;

import evm.main.comment.model.Comment;
import evm.main.event.model.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from comments as c where c.author_id = :author_id")
    List<Comment> findAllCommentsByAuthorId(@Param("author_id")Long authorId, Pageable pageable);

    @Query("select c from comments as c where c.event_id = :event_id")
    List<Comment> findAllCommentsByEventId(@Param("event_id")Long eventId, Pageable pageable);

    @Query("delete from comments as c where c.author_id := author_id")
    void deleteAllCommentsByAuthorId(@Param("author_id")Long authorId);

    @Query("delete from comments as c where c.event_id := event_id")
    void deleteAllCommentsByEventId(@Param("event_id")Long eventId);

}
