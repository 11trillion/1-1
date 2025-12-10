package com.oneonone.userservice.presentation;

import com.oneonone.common.response.ApiResponse;
import com.oneonone.userservice.application.command.*;
import com.oneonone.userservice.application.service.AuthService;
import com.oneonone.userservice.application.service.UserService;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.presentation.dto.request.*;
import com.oneonone.userservice.presentation.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "User API",
        description = "회원가입 및 로그인을 비롯한 사용자를 담당하는 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @Operation(
            summary = "회원가입",
            description = "회원가입을 통해 새로운 사용자를 생성합니다. 역할을 부여하지 않을 경우 USER로 기본 설정됩니다."
    )
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @Parameter(description = "사용자 생성 요청 정보", required = true)
            @Valid @RequestBody SignupRequest request) {
        SignupCommand command = new SignupCommand(
                request.username(),
                request.password(),
                request.nickname(),
                request.slackId(),
                request.role()
        );
        User user = userService.signUp(command);
        SignupResponse response = SignupResponse.from(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "회원가입 완료"));
    }

    @Operation(
            summary = "로그인",
            description = "회원가입 시 사용한 아이디와 비밀번호를 활용해 로그인하고, 토큰을 발급 받습니다."
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Parameter(description = "로그인 요청 정보", required = true)
            @Valid @RequestBody LoginRequest request) {
        LoginCommand command = new LoginCommand(
                request.username(),
                request.password()
        );
        LoginResponse response = authService.login(command);
        return ResponseEntity.ok(ApiResponse.success(response, "로그인 성공"));
    }

    @Operation(
            summary = "토큰 재발급",
            description = "RefreshToken을 활용해 토큰을 재발급 받습니다. 재발급 시 이용된 RefreshToken은 재사용이 불가합니다."
    )
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<LoginResponse>> reissue(
            @RequestHeader("Authorization") String refreshToken) {
        String token = refreshToken.replace("Bearer ", "");
        LoginResponse response = authService.reissue(token);
        return ResponseEntity.ok(ApiResponse.success(response, "토큰 재발급 성공"));
    }

    @Operation(
            summary = "로그아웃",
            description = "로그아웃합니다. AccessToken은 블랙리스트에 등록되어 더 이상 사용할 수 없습니다."
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String header) {
        String token = header.substring(7);
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 사용자의 정보를 조회합니다."
    )
    @PreAuthorize("hasAnyRole('USER', 'MASTER')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        UserResponse response = userService.getMyProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "내 정보 조회 성공"));
    }

    @Operation(
            summary = "내 정보 수정",
            description = "현재 로그인한 사용자의 정보(비밀번호, 닉네임, 슬랙 ID)를 수정합니다."
    )
    @PatchMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'MASTER')")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "수정하고자 하는 정보", required = true)
            @Valid @RequestBody UpdateUserRequest request) {
        UpdateUserCommand command = new UpdateUserCommand(
                request.password(),
                request.nickname(),
                request.slackId()
        );
        UserResponse response = userService.updateMyProfile(userId, command);
        return ResponseEntity.ok(ApiResponse.success(response, "내 정보 업데이트 성공"));
    }

    @Operation(
            summary = "회원 탈퇴",
            description = "현재 로그인한 사용자의 정보를 삭제함으로써(soft delete) 회원 탈퇴를 진행하며, 로그아웃 처리 됩니다."
    )
    @PreAuthorize("hasAnyRole('USER', 'MASTER')")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteMyProfile(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("Authorization") String header) {
        userService.deleteMyProfile(userId);
        authService.logout(header.substring(7));
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "사용자 목록 조회",
            description = "관리자가 모든 사용자의 목록을 조회합니다."
    )
    @PreAuthorize("hasRole('MASTER')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MasterUserResponse>>> getUserList(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MasterUserResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "사용자 목록 조회 성공"));
    }

    @Operation(
            summary = "사용자 정보 조회",
            description = "관리자가 특정 사용자의 정보를 조회합니다."
    )
    @PreAuthorize("hasRole('MASTER')")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<MasterUserResponse>> getUserDetail(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable Long userId) {
        MasterUserResponse response = userService.getUser(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "사용자 조회 성공"));
    }

    @Operation(
            summary = "사용자 정보 수정",
            description = "관리자가 특정 사용자의 정보(닉네임, 역할, 상태, 포인트 잔액, 슬랙 ID)를 수정합니다.")
    @PreAuthorize("hasRole('MASTER')")
    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<MasterUserResponse>> updateUser(
            @Parameter(description = "수정하고자 하는 정보", required = true)
            @RequestBody UpdateMasterRequest request,
            @Parameter(description = "수정할 사용자 ID", required = true)
            @PathVariable Long userId) {
        UpdateMasterCommand command = new UpdateMasterCommand(
                request.nickname(),
                request.role(),
                request.status(),
                request.pointBalance(),
                request.slackId()
        );
        MasterUserResponse response = userService.updateUser(userId, command);
        return ResponseEntity.ok(ApiResponse.success(response, "사용자 정보 업데이트 성공"));
    }

    @Operation(
            summary = "사용자 삭제",
            description = "관리자가 특정 사용자를 삭제합니다."
    )
    @PreAuthorize("hasRole('MASTER')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long id,
            @Parameter(description = "삭제할 사용자 ID", required = true)
            @PathVariable Long userId) {
        userService.deleteByMaster(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "사용자 포인트 잔액 조회 - 서비스 간 통신용",
            description = "관리자가 특정 사용자의 포인트 잔액을 조회합니다."
    )
    // TODO: 통신용은 Internal로 분리하면 좋을 듯
    @PreAuthorize("hasRole('MASTER')")
    @GetMapping("/{userId}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(
            @Parameter(description = "포인트 잔액을 조회할 사용자 ID", required = true)
            @PathVariable Long userId) {
        BalanceResponse response = userService.getPoint(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "사용자 포인트 조회 성공"));
    }

    @Operation(
            summary = "사용자 포인트 잔액 수정 - 서비스 간 통신용",
            description = "관리자가 특정 사용자의 포인트를 증가/감소시킵니다."
    )
    @PreAuthorize("hasRole('MASTER')")
    @PatchMapping("/{userId}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> updateBalance(
            @Parameter(description = "포인트 잔액을 수정할 사용자 ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "포인트 증감량(증가: 양수, 감소: 음수)", required = true)
            @RequestBody UpdateBalanceRequest request) {
        UUID sagaId = UUID.randomUUID();
        UpdateBalanceCommand command = new UpdateBalanceCommand(
                sagaId, // TODO: sagaId는 호출하는 쪽 (Betting Service)가 관리하도록 수정
                request.amount(),
                request.type(),
                UUID.randomUUID(),
                request.betId());
        BalanceResponse response = userService.updateBalance(userId, command);
        return ResponseEntity.ok(ApiResponse.success(response, "사용자 포인트 밸런스 수정 성공"));
    }
}
