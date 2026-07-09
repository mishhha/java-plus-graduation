package evm.main.users.repository;

import evm.main.users.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.List;

public interface UserRepositoryJpa extends JpaRepository<User, Long> {

    @Override
    List<User> findAllById(Iterable<Long> id);
    
    Page<User> findAll(Pageable pageable);

    boolean existsByEmail(String email);
}
