package com.oneonone.userservice.infrastructure.repository;

import com.oneonone.userservice.application.dto.UserInfo;
import com.oneonone.userservice.domain.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    Page<UserInfo> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<User> findByUserIdAndDeletedAtIsNull(Long userId);

    boolean existsByNicknameAndDeletedAtIsNull(String nickname);

    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    // 비관적 락 구현
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.userId = :userId and u.deletedAt is null")
    Optional<User> findByUserIdForUpdate(@Param("userId") Long userId);
}
