package com.oneonone.userservice.presentation;

import com.oneonone.common.enums.UserRole;
import com.oneonone.common.exception.BusinessException;
import com.oneonone.common.response.ApiResponse;
import com.oneonone.userservice.application.command.*;
import com.oneonone.userservice.application.service.AuthService;
import com.oneonone.userservice.application.service.UserService;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.exception.UserErrorCode;
import com.oneonone.userservice.presentation.dto.request.*;
import com.oneonone.userservice.presentation.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
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

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginCommand command = new LoginCommand(
                request.username(),
                request.password()
        );
        LoginResponse response = authService.login(command);
        return ResponseEntity.ok(ApiResponse.success(response, "로그인 성공"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @RequestHeader("X-User-Id") Long userId) {
        UserResponse response = userService.getMyProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "내 정보 조회 성공"));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateUserRequest request) {
        UpdateUserCommand command = new UpdateUserCommand(
                request.password(),
                request.nickname(),
                request.slackId()
        );
        UserResponse response = userService.updateMyProfile(userId, command);
        return ResponseEntity.ok(ApiResponse.success(response, "내 정보 업데이트 성공"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteMyProfile(
            @RequestHeader("X-User-Id") Long userId) {
        userService.deleteMyProfile(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MasterUserResponse>>> getUserList(
            @RequestHeader("X-User-Role") String role,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (!UserRole.valueOf(role).equals(UserRole.MASTER)) throw new BusinessException(UserErrorCode.FORBIDDEN);
        Page<MasterUserResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "사용자 목록 조회 성공"));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<MasterUserResponse>> getUserDetail(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long userId) {
        if (!UserRole.valueOf(role).equals(UserRole.MASTER)) throw new BusinessException(UserErrorCode.FORBIDDEN);
        MasterUserResponse response = userService.getUser(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "사용자 조회 성공"));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<MasterUserResponse>> updateUser(
            @RequestHeader("X-User-Role") String role,
            @RequestBody UpdateMasterRequest request,
            @PathVariable Long userId) {
        if (!UserRole.valueOf(role).equals(UserRole.MASTER)) throw new BusinessException(UserErrorCode.FORBIDDEN);
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

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @RequestHeader("X-User-Id") Long id,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long userId) {
        if (!UserRole.valueOf(role).equals(UserRole.MASTER)) throw new BusinessException(UserErrorCode.FORBIDDEN);
        userService.deleteByMaster(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long userId) {
        if (!UserRole.valueOf(role).equals(UserRole.MASTER)) throw new BusinessException(UserErrorCode.FORBIDDEN);
        BalanceResponse response = userService.getPoint(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "사용자 포인트 조회 성공"));
    }

    @PatchMapping("/{userId}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> updateBalance(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UpdateBalanceRequest request) {
//        if (!UserRole.valueOf(role).equals(UserRole.MASTER)) throw new BusinessException(UserErrorCode.FORBIDDEN);
        UpdateBalanceCommand command = new UpdateBalanceCommand(
                request.amount(),
                request.type(),
                request.eventId(),
                UUID.fromString("11111111-1111-1111-1111-111111111111")); // 임의의 random UUID
        BalanceResponse response = userService.updateBalance(userId, command);
        return ResponseEntity.ok(ApiResponse.success(response, "사용자 포인트 밸런스 수정 성공"));
    }
}
