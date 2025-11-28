package com.oneonone.userservice.infrastructure.repository;

import com.oneonone.userservice.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
}
