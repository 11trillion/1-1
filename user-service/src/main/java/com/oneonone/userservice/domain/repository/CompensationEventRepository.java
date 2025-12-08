package com.oneonone.userservice.domain.repository;

import com.oneonone.userservice.domain.entity.CompensationEvent;

import java.util.UUID;

public interface CompensationEventRepository {
    /**
     * 해당 원본 이벤트 ID에 대한 보상이 이미 처리되었는지 확인
     */
    boolean existsByOriginalEventId(UUID originalEventId);
    /**
     * 보상 이벤트 저장
     */
    CompensationEvent save(CompensationEvent record);
}
