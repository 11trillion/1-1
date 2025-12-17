package com.oneonone.userservice.application.strategy;

import com.oneonone.userservice.application.command.UpdateBalanceCommand;
import com.oneonone.userservice.presentation.dto.response.BalanceResponse;

public interface BalanceUpdateStrategy {
    String getType();
    BalanceResponse updateBalance(Long userId, UpdateBalanceCommand command);
}