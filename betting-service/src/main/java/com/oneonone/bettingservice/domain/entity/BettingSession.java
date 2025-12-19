package com.oneonone.bettingservice.domain.entity;

import com.oneonone.bettingservice.domain.vo.BetResult;
import com.oneonone.common.enums.GameResult;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("bets")
public class BettingSession implements Serializable {
    private UUID gameId;
    private UUID betId;
    private Long userId;
    private BigDecimal betAmount;
    private BigDecimal odds;
    private GameResult betType;
    private BetResult betResult;
}
