package com.oneonone.userservice.application.command;

public record LoginCommand(
        String username,
        String password
) {
}