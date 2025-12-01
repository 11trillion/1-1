package com.oneonone.userservice.domain.repository;

import com.oneonone.userservice.application.dto.UserInfo;
import com.oneonone.userservice.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface UserRepository {
    User save(User user);

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByUserIdAndDeletedAtIsNull(Long userId);

    boolean existsByNickname(String nickname);

    Page<UserInfo> findAllByDeletedAtIsNull(Pageable pageable);
}
