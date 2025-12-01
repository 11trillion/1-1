package com.oneonone.bettingservice.domain;

import lombok.Getter;

@Getter
public enum BetType {
    WIN("승"),
    LOSE("패"),
    DRAW("무승부")
    ;

    private final String betResult;

    BetType(String betType) {
        this.betResult = betType;
    }
}
