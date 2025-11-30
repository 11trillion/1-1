package com.oneonone.userservice.application.service;

import com.oneonone.common.exception.BusinessException;
import com.oneonone.userservice.application.command.SignupCommand;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.domain.repository.UserRepository;
import com.oneonone.userservice.exception.UserErrorCode;
import com.oneonone.userservice.presentation.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User signUp(SignupCommand command) {
        if (userRepository.existsByUsername(command.username())) throw new BusinessException(UserErrorCode.DUPLICATE_USER);
        String encodedPassword = passwordEncoder.encode(command.password());

        User user = User.create(
                command.username(),
                encodedPassword,
                command.nickname(),
                command.slackId(),
                command.role()
        );
        return userRepository.save(user);
    }

    public UserResponse getMyProfile(Long userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }
}
