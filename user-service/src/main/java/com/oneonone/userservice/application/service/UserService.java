package com.oneonone.userservice.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.common.exception.BusinessException;
import com.oneonone.userservice.application.command.SignupCommand;
import com.oneonone.userservice.application.command.UpdateBalanceCommand;
import com.oneonone.userservice.application.command.UpdateMasterCommand;
import com.oneonone.userservice.application.command.UpdateUserCommand;
import com.oneonone.userservice.application.dto.UserInfo;
import com.oneonone.userservice.domain.entity.OutboxEvent;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.domain.repository.OutboxRepository;
import com.oneonone.userservice.domain.repository.UserRepository;
import com.oneonone.userservice.exception.UserErrorCode;
import com.oneonone.userservice.infrastructure.kafka.dto.BalanceEventPayload;
import com.oneonone.userservice.presentation.dto.response.BalanceResponse;
import com.oneonone.userservice.presentation.dto.response.MasterUserResponse;
import com.oneonone.userservice.presentation.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

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

    public BalanceResponse getPoint(Long userId) {
        User user = findUserById(userId);
        UserInfo userInfo = UserInfo.from(user);
        return BalanceResponse.from(userInfo);
    }

//    @Transactional
//    public BalanceResponse updatePoint(Long userId, UpdatePointCommand command) {
//        User user = findUserById(userId);
//        user.updatePoint(command.amount());
//        UserInfo userInfo = UserInfo.from(user);
//        return BalanceResponse.from(userInfo);
//    }

    private User findUserById(Long userId) {
        return userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    @Transactional // <- 이 부분 추가했어여
    public BalanceResponse updateBalance(Long userId, UpdateBalanceCommand command) {
        User user = findUserById(userId);

        // Balance 업데이트
        // 성능 개선을 위해 락 없이 먼저 구현, 나중에 낙관적/비관적 락 구현
        user.updateBalance(command.amount(), command.type());
//
//        // 3. Outbox payload 생성
//        Map<String, Object> payloadMap = new HashMap<>();
//        payloadMap.put("userId", userId);
//        payloadMap.put("amount", command.amount());
//        payloadMap.put("type", command.type());
//        payloadMap.put("eventId", command.eventId());
//        if (command.betId() != null) {
//            payloadMap.put("betId", command.betId());
//        }

        BalanceEventPayload payloadDTO = new BalanceEventPayload(
                command.eventId().toString(),
                userId,
                command.amount(),
                command.type(),
                command.betId().toString()
        );

        String payload;
        try {
            payload = objectMapper.writeValueAsString(payloadDTO);
            log.info("[BALANCE-UPDATE] Created payload: {}", payload);
        } catch (JsonProcessingException e) {
            log.error("[BALANCE-UPDATE] Failed to serialize payload: {}", e);
            throw new BusinessException(UserErrorCode.OUTBOX_PAYLOAD_ERROR);
        }

        // outbox 이벤트 생성 및 저장
        OutboxEvent outboxEvent = new OutboxEvent(
                command.eventId(),
                userId,
                payload
        );
        outboxRepository.save(outboxEvent);

        return new BalanceResponse(userId, user.getPointBalance());
    }
}
