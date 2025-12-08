package com.oneonone.bettingservice.domain;

import lombok.Getter;

@Getter
public enum BetType {
    HOME_WIN("홈 팀 승리"),
    AWAY_WIN( "어웨이 팀 승리"),
    DRAW("비김"),
    WAIT("경기 시작 전");
    ;

    private final String betType;

    BetType(String betType) {
        this.betType = betType;
    }
}
