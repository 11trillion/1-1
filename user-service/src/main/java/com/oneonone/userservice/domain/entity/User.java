package com.oneonone.userservice.domain.entity;

import com.oneonone.common.enums.UserRole;
import com.oneonone.common.model.BaseEntity;
import com.oneonone.userservice.domain.enums.UserStatus;
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
}
