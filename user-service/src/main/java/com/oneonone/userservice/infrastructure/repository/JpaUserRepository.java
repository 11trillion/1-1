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

    /**
     * 낙관적 락 - BaseEntity의 @Version 필드 자동 체크
     * 조회 시 version을 읽고, 업데이트 시 version이 동일한지 확인
     */
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.deletedAt IS NULL")
    Optional<User> findByIdWithOptimisticLock(@Param("userId") Long userId);

    /**
     * 비관적 락 - SELECT ... FOR UPDATE
     * 트랜잭션이 끝날 때까지 해당 row를 다른 트랜잭션이 수정하지 못하도록 잠금
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.deletedAt IS NULL")
    Optional<User> findByIdWithPessimisticLock(@Param("userId") Long userId);
}
