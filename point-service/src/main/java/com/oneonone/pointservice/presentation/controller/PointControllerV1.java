package com.oneonone.pointservice.presentation.controller;

import com.oneonone.common.response.ApiResponse;
import com.oneonone.pointservice.application.PointServiceV1;
import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.presentation.request.CreatePointRequest;
import com.oneonone.pointservice.presentation.request.UpdatePointStatusRequest;
import com.oneonone.pointservice.presentation.response.PointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
