package com.oneonone.userservice.application.command;

public record EmailVerifyCommand(
        String email,
        String code
) {}