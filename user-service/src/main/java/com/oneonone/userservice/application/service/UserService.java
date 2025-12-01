package com.oneonone.userservice.application.service;

import com.oneonone.common.exception.BusinessException;
import com.oneonone.userservice.application.command.SignupCommand;
import com.oneonone.userservice.application.command.UpdateMasterCommand;
import com.oneonone.userservice.application.command.UpdateUserCommand;
import com.oneonone.userservice.application.dto.UserInfo;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.domain.repository.UserRepository;
import com.oneonone.userservice.exception.UserErrorCode;
import com.oneonone.userservice.presentation.dto.request.UpdateMasterRequest;
import com.oneonone.userservice.presentation.dto.response.MasterUserResponse;
import com.oneonone.userservice.presentation.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        User user = findUserById(userId);
        UserInfo userInfo = UserInfo.from(user);
        return UserResponse.from(userInfo);
    }

    @Transactional
    public UserResponse updateMyProfile(Long userId, UpdateUserCommand command) {
        User user = findUserById(userId);
        if (command.nickname() != null && userRepository.existsByNicknameAndDeletedAtIsNull(command.nickname())) {
            throw new BusinessException(UserErrorCode.INVALID_NICKNAME);
        }
        String encodedPw = command.password() != null ? passwordEncoder.encode(command.password()) : null;
        user.updateMyProfile(
                encodedPw,
                command.nickname(),
                command.slackId());
        UserInfo userInfo = UserInfo.from(user);
        return UserResponse.from(userInfo);
    }

    @Transactional
    public void deleteMyProfile(Long userId) {
        User user = findUserById(userId);
        user.softDelete(userId);
    }

    public Page<MasterUserResponse> getAllUsers(Pageable pageable) {
        Page<UserInfo> users = userRepository.findAllByDeletedAtIsNull(pageable);
        return users.map(MasterUserResponse::from);
    }

    public MasterUserResponse getUser(Long userId) {
        User user = findUserById(userId);
        UserInfo userInfo = UserInfo.from(user);
        return MasterUserResponse.from(userInfo);
    }

    @Transactional
    public MasterUserResponse updateUser(Long userId, UpdateMasterCommand command) {
        User user = findUserById(userId);
        if (command.nickname() != null && userRepository.existsByNicknameAndDeletedAtIsNull(command.nickname())) {
            throw new BusinessException(UserErrorCode.INVALID_NICKNAME);
        }
        user.updateByMaster(
                command.nickname(),
                command.role(),
                command.status(),
                command.pointBalance(),
                command.slackId()
        );
        UserInfo userInfo = UserInfo.from(user);
        return MasterUserResponse.from(userInfo);
    }

    @Transactional
    public void deleteByMaster(Long id, Long userId) {
        User user = findUserById(userId);
        user.softDelete(id);
    }

    private User findUserById(Long userId) {
        return userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }
}
