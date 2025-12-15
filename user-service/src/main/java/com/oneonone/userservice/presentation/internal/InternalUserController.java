package com.oneonone.userservice.presentation.internal;

import com.oneonone.common.response.ApiResponse;
import com.oneonone.userservice.application.command.UpdateBalanceCommand;
import com.oneonone.userservice.application.service.UserService;
import com.oneonone.userservice.presentation.dto.request.UpdateBalanceRequest;
import com.oneonone.userservice.presentation.dto.response.BalanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Internal API - User",
        description = "마이크로서비스 간 통신 전용 API (외부 노출 금지)"
)
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/users")
public class InternalUserController {

    private final UserService userService;

    @Operation(
            summary = "포인트 잔액 수정 - 서비스 간 통신 전용",
            description = """
            Betting Service 등 다른 마이크로서비스가 사용자의 포인트를 수정합니다.
            - sagaId는 호출하는 서비스(Betting Service)에서 생성하여 전달
            - 내부 네트워크에서만 접근 가능
            - Service-to-Service 인증 필요
            """
    )
    @PatchMapping("/{userId}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> updateBalance(
            @Parameter(description = "포인트 잔액을 수정할 사용자 ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "포인트 증감량(증가: 양수, 감소: 음수)", required = true)
            @Valid @RequestBody UpdateBalanceRequest request) {

        log.info("[INTERNAL-API] Balance update request - sagaId={}, userId={}, amount={}, type={}",
                request.sagaId(), userId, request.amount(), request.type());

        // Saga ID 검증
        if (request.sagaId() == null) {
            log.error("[INTERNAL-API] sagaId is required for internal API calls");
            throw new IllegalArgumentException("sagaId는 필수입니다 (호출하는 서비스에서 생성 필요)");
        }

        UpdateBalanceCommand command = new UpdateBalanceCommand(
                request.sagaId(),        // TODO: sagaId는 호출하는 쪽 (Betting Service)가 관리하도록 수정
                request.amount(),
                request.type(),
                UUID.randomUUID(),       // eventId는 여기서 생성
                request.betId()
        );

        BalanceResponse response = userService.updateBalance(userId, command);

        log.info("[INTERNAL-API] Balance updated - sagaId={}, userId={}, newBalance={}",
                request.sagaId(), userId, response.pointBalance());

        return ResponseEntity.ok(ApiResponse.success(response, "포인트 수정 완료"));
    }

    @Operation(
            summary = "포인트 잔액 조회 - 서비스 간 통신 전용",
            description = "다른 마이크로서비스가 사용자의 포인트 잔액을 조회합니다."
    )
    @GetMapping("/{userId}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId) {

        log.info("[INTERNAL-API] Balance query - userId={}", userId);

        BalanceResponse response = userService.getPoint(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "포인트 잔액 조회 완료"));
    }
}