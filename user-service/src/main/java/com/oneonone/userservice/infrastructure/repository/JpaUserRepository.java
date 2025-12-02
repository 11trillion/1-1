package com.oneonone.userservice.infrastructure.repository;

import com.oneonone.userservice.application.dto.UserInfo;
import com.oneonone.userservice.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    Page<UserInfo> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<User> findByUserIdAndDeletedAtIsNull(Long userId);

    boolean existsByNicknameAndDeletedAtIsNull(String nickname);

    Optional<User> findByUsernameAndDeletedAtIsNull(String username);
}
