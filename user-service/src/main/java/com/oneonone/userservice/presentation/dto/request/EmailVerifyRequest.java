package com.oneonone.userservice.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EmailVerifyRequest(
        @NotBlank String email,
        @NotBlank String code
) {}