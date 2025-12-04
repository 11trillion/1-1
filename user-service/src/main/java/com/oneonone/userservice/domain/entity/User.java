package com.oneonone.userservice.domain.entity;

import com.oneonone.common.enums.UserRole;
import com.oneonone.common.exception.BusinessException;
import com.oneonone.common.model.BaseEntity;
import com.oneonone.userservice.domain.enums.UserStatus;
import com.oneonone.userservice.exception.UserErrorCode;
import com.oneonone.userservice.infrastructure.kafka.event.BalanceCompensationEvent;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "p_users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false)
    private Long pointBalance;

    @Column
    private String slackId;

    public static User create(String username,
                              String encodedPassword,
                              String nickname,
                              String slackId,
                              UserRole role) {
        return User.builder()
                .username(username)
                .password(encodedPassword)
                .nickname(nickname)
                .status(UserStatus.ACTIVE)
                .role(role == null ? UserRole.USER : role)
                .pointBalance(0L)
                .slackId(slackId)
                .build();
    }

    public void updateMyProfile(String password,
                              String nickname,
                              String slackId) {
        if (password != null && !password.isBlank()) this.password = password;
        if (nickname != null) {
            validate(nickname);
            this.nickname = nickname;
        }
        if (slackId != null) this.slackId = slackId;
    }

    public void updateByMaster(String nickname,
                               UserRole role,
                               UserStatus status,
                               Long pointBalance,
                               String slackId) {
        if (nickname != null) {
            validate(nickname);
            this.nickname = nickname;
        }
        if (role != null) this.role = role;
        if (status != null) this.status = status;
        if (pointBalance != null) {
            if (pointBalance < 0) throw new BusinessException(UserErrorCode.INVALID_POINT);
            this.pointBalance = pointBalance;
        }
        if (slackId != null) this.slackId = slackId;
    }

    public void updateBalance(Long amount, String type) {
        if ("DEBIT".equalsIgnoreCase(type)) {
            if (this.pointBalance - amount < 0) {
                throw new BusinessException(UserErrorCode.INVALID_POINT);
            }
            this.pointBalance -= amount;
        } else if ("CREDIT".equalsIgnoreCase(type)) {
            this.pointBalance += amount;
        } else {
            throw new BusinessException(UserErrorCode.INVALID_POINT_TYPE);
        }
    }

    public void compensateBalance(BalanceCompensationEvent event) {
        rollbackBalance(event.amount(), event.type());
    }

    public void rollbackBalance(Long amount, String type) {
        if ("DEBIT".equalsIgnoreCase(type)) {
            this.pointBalance += amount;
        }
        else if ("CREDIT".equalsIgnoreCase(type)) {
            if (this.pointBalance - amount < 0) {
                throw new BusinessException(UserErrorCode.INVALID_POINT);
            }
            this.pointBalance -= amount;
        }
        else {
            throw new BusinessException(UserErrorCode.INVALID_POINT_TYPE);
        }
    }

    public void validate(String nickname) {
        if (nickname.length() < 2) throw new BusinessException(UserErrorCode.INVALID_NICKNAME);
        // TODO: slack ID 등 검증 필요 시 추가
    }
}
