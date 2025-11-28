package com.oneonone.userservice.presentation;

import com.oneonone.common.response.ApiResponse;
import com.oneonone.userservice.application.command.SignupCommand;
import com.oneonone.userservice.application.service.UserService;
import com.oneonone.userservice.domain.entity.User;
import com.oneonone.userservice.presentation.dto.request.SignupRequest;
import com.oneonone.userservice.presentation.dto.response.SignupResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupCommand command = new SignupCommand(
                request.getUsername(),
                request.getPassword(),
                request.getNickname(),
                request.getSlackId()
        );
        User user = userService.signUp(command);
        SignupResponse response = SignupResponse.from(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "회원가입이 완료되었습니다."));
    }
}
