package com.oneonone.userservice.infrastructure.repository;

import com.oneonone.userservice.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByUserId(Long userId);
}
