package com.oneonone.userservice.application.command;

public record SignupCommand(
        String username,
        String password,
        String nickname,
        String slackId
) {
}
