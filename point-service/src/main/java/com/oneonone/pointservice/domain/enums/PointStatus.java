package com.oneonone.pointservice.domain.enums;

public enum PointStatus {
    PENDING,          // 대기 중
    PROCESSING,       // 처리 중
    SUCCESS,          // 성공 (베팅 확정)
    FAILED,           // 실패
    COMPENSATING,     // 보상 처리 중
    COMPENSATED       // 보상 완료 (베팅 취소/환불)
}