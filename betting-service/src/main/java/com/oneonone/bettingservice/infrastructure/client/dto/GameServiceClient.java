package com.oneonone.bettingservice.infrastructure.client.dto;

import com.oneonone.bettingservice.infrastructure.client.config.FeignClientConfig;
import com.oneonone.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "game-service",
        configuration = FeignClientConfig.class
)
public interface GameServiceClient {
    @GetMapping("/api/v1/games/{gameId}")
    ApiResponse<GameResponse> getGameById(
            @PathVariable UUID gameId
    );

}
