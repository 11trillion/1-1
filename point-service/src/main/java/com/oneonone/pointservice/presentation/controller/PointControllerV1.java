package com.oneonone.pointservice.presentation.controller;

import com.oneonone.common.response.ApiResponse;
import com.oneonone.pointservice.application.PointServiceV1;
import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.presentation.request.CreatePointRequest;
import com.oneonone.pointservice.presentation.request.UpdatePointStatusRequest;
import com.oneonone.pointservice.presentation.response.PointResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ApiResponse<Map<String, Object>> getPoints(
            @RequestParam Long userId,
            @RequestParam(required = false) String status,
            Pageable pageable) {

        Page<PointResponse> page = pointServiceV1.getPoints(userId, status, pageable);

        Map<String, Object> result = Map.of(
                "content", page.getContent(),
                "pageInfo", Map.of(
                        "page", page.getNumber(),
                        "size", page.getSize(),
                        "totalElements", page.getTotalElements(),
                        "totalPages", page.getTotalPages(),
                        "hasNext", page.hasNext()
                )
        );

        return ApiResponse.success(result, "포인트 조회 성공");
    }
}
