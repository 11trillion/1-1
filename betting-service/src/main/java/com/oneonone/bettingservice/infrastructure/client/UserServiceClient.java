package com.oneonone.bettingservice.infrastructure.client;

import com.oneonone.bettingservice.infrastructure.client.dto.BalanceResponse;
import com.oneonone.bettingservice.infrastructure.client.dto.UpdateBalanceRequest;
import com.oneonone.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "user-service"
)
public interface UserServiceClient {

    @GetMapping("/api/v1/internal/users/{userId}/balance")
    ApiResponse<BalanceResponse> getBalance(
            @PathVariable Long userId,
            @RequestHeader("X-Service-Name") String serviceName,
            @RequestHeader("X-Internal-Request") String internalRequest
    );

    @PatchMapping("/api/v1/internal/users/{userId}/balance")
    ApiResponse<BalanceResponse> updateBalance(
            @PathVariable Long userId,
            @RequestBody UpdateBalanceRequest request
    );
}
