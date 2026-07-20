package evm.main.comment.repository;

import evm.main.comment.model.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEventIdOrderByPublishedDateDesc(Long eventId, Pageable pageable);

    List<Comment> findAllByAuthorIdOrderByPublishedDateDesc(Long authorId, Pageable pageable);

    void deleteAllByAuthorId(Long authorId);

    void deleteAllByEventId(Long eventId);
}
