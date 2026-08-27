package evm.comment.repository;

import evm.comment.model.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    //@EntityGraph подгружает author одним запросом, вместо N отдельных запросов каждого комментария
    @EntityGraph(attributePaths = "author")
    List<Comment> findAllByEventIdOrderByCreatedDesc(Long eventId, Pageable pageable);

    @EntityGraph(attributePaths = "author")
    List<Comment> findAllByAuthorIdOrderByCreatedDesc(Long authorId, Pageable pageable);

    void deleteAllByAuthorId(Long authorId);

    void deleteAllByEventId(Long eventId);
}
