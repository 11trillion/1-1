package com.oneonone.gameservice.domain.entity;

import lombok.Getter;

@Getter
public enum GameResult {
    HOME_WIN("홈 팀 승리"),
    AWAY_WIN( "어웨이 팀 승리"),
    DRAW("비김"),
    WAIT("경기 시작 전");

    private final String description;

    GameResult(String description) {
        this.description = description;
    }

    public boolean isWait() {
        return this == WAIT;
    }
    public static GameResult checkScore(int homeScore, int awayScore) {
        if (homeScore > awayScore) return HOME_WIN;
        if (homeScore < awayScore) return AWAY_WIN;
        return DRAW;
    }


}
