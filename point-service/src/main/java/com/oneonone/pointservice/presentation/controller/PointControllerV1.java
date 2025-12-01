package com.oneonone.pointservice.presentation.controller;

import com.oneonone.common.exception.BusinessException;
import com.oneonone.common.response.ApiResponse;
import com.oneonone.pointservice.application.PointServiceV1;
import com.oneonone.pointservice.domain.PointErrorCode;
import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.domain.enums.PointStatus;
import com.oneonone.pointservice.presentation.request.CreatePointRequest;
import com.oneonone.pointservice.presentation.request.UpdatePointStatusRequest;
import com.oneonone.pointservice.presentation.response.PointResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/points")
public class PointControllerV1 {
    private final PointServiceV1 pointServiceV1;;

    @PostMapping
    public ApiResponse<PointResponse> createPoint(@RequestBody CreatePointRequest request){
        Point point = pointServiceV1.createPoint(
                request.getPointType(),
                request.getAmount(),
                request.getDescription(),
                request.getUserId()
        );
        PointResponse pointResponse = PointResponse.from(point);
        return ApiResponse.success(pointResponse, "포인트 생성 성공");
    }

    @PatchMapping("/{pointId}/status")
    public ApiResponse<PointResponse> updatePointStatus(
            @PathVariable("pointId") UUID pointId,
            @RequestBody UpdatePointStatusRequest request
    ) {
        Point updatedPoint = pointServiceV1.updatePointStatus(pointId, request.getStatus());
        PointResponse pointResponse = PointResponse.from(updatedPoint);
        return ApiResponse.success(pointResponse, "포인트 상태 변경 성공");
    }

    @GetMapping
    public ApiResponse<Page<PointResponse>> getPoints(
            @RequestParam Long userId,
            @RequestParam(required = false)  String status,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        PointStatus pointStatus = null;

        if (status != null) {
            if (status.isBlank()) {
                throw new BusinessException(PointErrorCode.INVALID_STATUS);
            }
            try {
                pointStatus = PointStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(PointErrorCode.INVALID_STATUS);
            }
        }

        Page<PointResponse> result =
                pointServiceV1.getPoints(userId, pointStatus, pageable);

        return ApiResponse.success(result, "포인트 조회 성공");
    }
}
