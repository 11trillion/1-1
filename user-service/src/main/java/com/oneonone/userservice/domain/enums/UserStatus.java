package com.oneonone.userservice.domain.enums;

public enum UserStatus {
    PENDING, // 승인 대기
    APPROVE, // 승인 완료
    REJECTED, // 승인 거절
    BLOCKED // 정지된 사용자
}