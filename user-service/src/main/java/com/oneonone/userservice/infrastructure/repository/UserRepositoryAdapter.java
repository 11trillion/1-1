package com.oneonone.userservice.infrastructure.repository;

import com.oneonone.userservice.application.dto.UserInfo;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaUserRepository.existsByUsername(username);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaUserRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByUserIdAndDeletedAtIsNull(Long userId) {
        return jpaUserRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    @Override
    public boolean existsByNicknameAndDeletedAtIsNull(String nickname) {
        return jpaUserRepository.existsByNicknameAndDeletedAtIsNull(nickname);
    }

    @Override
    public Page<UserInfo> findAllByDeletedAtIsNull(Pageable pageable) {
        return jpaUserRepository.findAllByDeletedAtIsNull(pageable);
    }
}
