package com.oneonone.pointservice.presentation.controller;

import com.oneonone.common.response.ApiResponse;
import com.oneonone.pointservice.application.PointServiceV1;
import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.presentation.request.CreatePointRequest;
import com.oneonone.pointservice.presentation.response.PointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
