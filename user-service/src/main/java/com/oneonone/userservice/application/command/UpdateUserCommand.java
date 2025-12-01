package com.oneonone.userservice.application.command;

public record UpdateUserCommand(
        String password,
        String nickname,
        String slackId
) {
}
