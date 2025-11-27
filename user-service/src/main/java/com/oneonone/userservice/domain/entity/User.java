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
    private UserStatus status;

    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private Long pointBalance;

    @Column
    private String slackId;
}
