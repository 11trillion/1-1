package com.oneonone.userservice.domain.repository;

import com.oneonone.userservice.domain.entity.User;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface UserRepository {
    User save(User user);

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByUserId(Long userId);
}
