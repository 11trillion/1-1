package com.oneonone.bettingservice.domain;

import lombok.Getter;

@Getter
public enum BetResult {
    WIN("승"),
    LOSE("패"),
    DRAW("무승부"),
    PENDING("미정")
    ;

    private final String betResult;

    BetResult(String betResult) {
        this.betResult = betResult;
    }
}
