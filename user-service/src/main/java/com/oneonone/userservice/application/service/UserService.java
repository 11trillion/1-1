package com.oneonone.userservice.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneonone.common.exception.BusinessException;
import com.oneonone.userservice.application.command.*;
import com.oneonone.userservice.application.dto.UserInfo;
import com.oneonone.userservice.application.event.BalanceEventPayload;
import com.oneonone.userservice.domain.entity.OutboxEvent;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.domain.repository.OutboxRepository;
import com.oneonone.userservice.domain.repository.UserRepository;
import com.oneonone.userservice.exception.UserErrorCode;
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

import java.util.Optional;

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
                command.email(),
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

    @Transactional
    public BalanceResponse updateBalance(Long userId, UpdateBalanceCommand command) {
        // 멱등성 체크 - 이미 처리된 eventId인지 확인
        Optional<OutboxEvent> existingEvent = outboxRepository.findByEventId(command.eventId());
        if (existingEvent.isPresent()) {
            log.info("[BALANCE-UPDATE] Duplicate eventId detected: {}, returning existing result", command.eventId());
            User user = findUserById(userId);
            return new BalanceResponse(userId, user.getPointBalance());
        }

        // 성능 개선을 위해 락 없이 먼저 구현, 나중에 낙관적/비관적 락 구현
//        User user = findUserById(userId);

        // 비관적 락으로 사용자 조회
        // 포인트 차감은 동시성 충돌 가능성이 높아
        // PESSIMISTIC_WRITE 락을 사용하여
        // 동일 사용자에 대한 Balance 업데이트를 직렬화함
        User user = userRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        // Balance 업데이트
        user.updateBalance(command.amount(), command.type());
        log.info("[BALANCE-UPDATE] Balance updated. userId={}, balance={}",
                userId, user.getPointBalance());

        // Outbox payload 생성
        BalanceEventPayload event = new BalanceEventPayload(
                command.sagaId().toString(),
                command.eventId().toString(),
                userId,
                command.amount(),
                command.type(),
                command.betId() != null ? command.betId().toString() : null
        );

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
            log.info("[BALANCE-UPDATE] Created payload: {}", payload);
        } catch (JsonProcessingException e) {
            log.error("[BALANCE-UPDATE] Failed to serialize payload: {}", e);
            throw new BusinessException(UserErrorCode.OUTBOX_PAYLOAD_ERROR);
        }

        // outbox 이벤트 생성 및 저장
        OutboxEvent outboxEvent = new OutboxEvent(
                command.sagaId(),
                command.eventId(),
                userId,
                payload
        );
        outboxRepository.save(outboxEvent);

        log.info("[BALANCE-UPDATE] Successfully updated balance for userId: {}, new balance: {}",
                userId, user.getPointBalance());

        return new BalanceResponse(userId, user.getPointBalance());
    }
}