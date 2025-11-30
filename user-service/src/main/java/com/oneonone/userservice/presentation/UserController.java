package com.oneonone.userservice.presentation;

import com.oneonone.common.response.ApiResponse;
import com.oneonone.userservice.application.command.LoginCommand;
import com.oneonone.userservice.application.command.SignupCommand;
import com.oneonone.userservice.application.command.UpdateUserCommand;
import com.oneonone.userservice.application.service.AuthService;
import com.oneonone.userservice.application.service.UserService;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.presentation.dto.request.LoginRequest;
import com.oneonone.userservice.presentation.dto.request.SignupRequest;
import com.oneonone.userservice.presentation.dto.request.UpdateUserRequest;
import com.oneonone.userservice.presentation.dto.response.LoginResponse;
import com.oneonone.userservice.presentation.dto.response.SignupResponse;
import com.oneonone.userservice.presentation.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody UpdateUserRequest request) {
        UpdateUserCommand command = new UpdateUserCommand(
                request.password(),
                request.nickname(),
                request.slackId()
        );
        UserResponse response = userService.updateMyProfile(userId, command);
        return ResponseEntity.ok(ApiResponse.success(response, "내 정보 업데이트 성공"));
    }
}
