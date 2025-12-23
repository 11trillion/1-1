package com.oneonone.pointservice.presentation.controller;

import com.oneonone.common.response.ApiResponse;
import com.oneonone.pointservice.application.command.CreatePointCommand;
import com.oneonone.pointservice.application.command.PointCommandService;
import com.oneonone.pointservice.application.command.UpdatePointStatusCommand;
import com.oneonone.pointservice.application.query.PointQueryService;
import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.presentation.dto.request.CreatePointRequest;
import com.oneonone.pointservice.presentation.dto.request.UpdatePointStatusRequest;
import com.oneonone.pointservice.presentation.dto.response.PointResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Point API",
        description = "포인트 생성, 조회, 상태 변경을 담당하는 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/points")
public class PointController {
    private final PointCommandService pointCommandService; // 쓰기
    private final PointQueryService pointQueryService;     // 조회

    // ======================
    // 포인트 생성
    // ======================
    @PostMapping
    @Operation(summary = "포인트 생성", description = "포인트를 생성합니다. 생성된 포인트는 기본적으로 PENDING 상태입니다.")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<PointResponse>> createPoint(
            @Parameter(description = "포인트 생성 요청 정보", required = true)
            @RequestBody CreatePointRequest request
    ) {
        // CreatePointCommand 생성
        CreatePointCommand command = new CreatePointCommand(
                request.getPointType(),
                request.getAmount(),
                request.getDescription(),
                request.getUserId()
        );

        Point point = pointCommandService.createPoint(command);
        return ResponseEntity.ok(ApiResponse.success(PointResponse.from(point), "포인트 생성 성공"));
    }

    // ======================
    // 포인트 상태 변경
    // ======================
    @PatchMapping("/{pointId}/status")
    @Operation(
            summary = "포인트 상태 변경",
            description = "포인트의 상태를 변경합니다. SUCCESS 상태인 포인트는 변경할 수 없습니다."
    )
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<PointResponse>> updatePointStatus(
            @Parameter(description = "포인트 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa2")
            @PathVariable UUID pointId,

            @Parameter(description = "변경할 포인트 상태", required = true)
            @RequestBody UpdatePointStatusRequest request
    ) {
        UpdatePointStatusCommand command = new UpdatePointStatusCommand(
                pointId,
                request.getStatus()
        );

        Point updatedPoint = pointCommandService.updatePointStatus(command);
        return ResponseEntity.ok(ApiResponse.success(PointResponse.from(updatedPoint), "포인트 상태 변경 성공"));
    }

    // ======================
    // 포인트 조회
    // ======================
    @GetMapping
    @Operation(
            summary = "포인트 조회",
            description = "사용자의 포인트 내역을 조회합니다. 상태(PENDING, SUCCESS, FAILED)로 필터링할 수 있습니다."
    )
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<Page<PointResponse>>> getPoints(
            @Parameter(description = "사용자 ID", example = "1001", required = true)
            @RequestParam Long userId,

            @Parameter(description = "포인트 상태 (PENDING, SUCCESS, FAILED)", example = "PENDING")
            @RequestParam(required = false) String status,

            @Parameter(hidden = true)
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        Page<PointResponse> page = pointQueryService.getPoints(userId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "포인트 조회 성공"));
    }
}
