package com.fullnestjob.modules.users.repo;

import com.fullnestjob.modules.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
	Optional<User> findByEmail(String email);
    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<User> findByNameContainingIgnoreCaseAndEmailContainingIgnoreCase(String name, String email, Pageable pageable);
}


