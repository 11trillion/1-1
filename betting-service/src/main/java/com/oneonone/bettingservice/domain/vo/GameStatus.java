package com.oneonone.bettingservice.domain.vo;

import lombok.Getter;

@Getter
public enum GameStatus {
    SCHEDULED("경기 예정"),
    PROGRESS("경기 진행 중 "),
    DELAYED("경기 지연"),
    END("경기 종료");

    private final String description;

    GameStatus(String description) {
        this.description = description;
    }

    public boolean isScheduled() {
        return this == SCHEDULED;
    }
}
