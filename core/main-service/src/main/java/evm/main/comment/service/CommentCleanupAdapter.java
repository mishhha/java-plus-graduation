package evm.main.comment.service;

import evm.comment.repository.CommentRepository;
import evm.common.port.CommentCleanupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CommentCleanupAdapter implements CommentCleanupPort {

    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public void deleteAllByAuthorId(Long authorId) {
        commentRepository.deleteAllByAuthorId(authorId);
    }
}