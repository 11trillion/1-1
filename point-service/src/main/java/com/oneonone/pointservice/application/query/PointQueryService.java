package com.oneonone.pointservice.application.query;


import com.oneonone.common.exception.BusinessException;
import com.oneonone.pointservice.domain.PointErrorCode;
import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.domain.enums.PointStatus;
import com.oneonone.pointservice.domain.repository.PointRepository;
import com.oneonone.pointservice.presentation.dto.response.PointResponse;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointQueryService {

    private final PointRepository pointRepository;

    public Page<PointResponse> getPoints(Long userId, String status, Pageable pageable) {
        PointStatus pointStatus = parseStatus(status);

        Page<Point> page = (pointStatus != null)
                ? pointRepository.findByUserIdAndStatus(userId, pointStatus, pageable)
                : pointRepository.findByUserId(userId, pageable);

        return page.map(PointResponse::from);
    }

    private PointStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return PointStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(PointErrorCode.INVALID_STATUS);
        }
    }
}