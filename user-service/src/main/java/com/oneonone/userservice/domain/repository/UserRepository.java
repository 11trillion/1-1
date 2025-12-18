package com.oneonone.userservice.domain.repository;

import com.oneonone.userservice.application.dto.UserInfo;
import com.oneonone.userservice.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository {
    User save(User user);

    boolean existsByUsername(String username);

    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    Optional<User> findByUserIdAndDeletedAtIsNull(Long userId);

    boolean existsByNicknameAndDeletedAtIsNull(String nickname);

    Page<UserInfo> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<User> findByUserIdForUpdate(Long userId);
}
