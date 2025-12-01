package com.oneonone.bettingservice.domain;

import com.oneonone.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "p_bettings")
public class Betting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "bet_id")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "game_id", nullable = false)
    private UUID gameId;

    @Column(name = "bet_amount", nullable = false)
    private Integer betAmount;

    @Column(name = "odds", nullable = false)
    private BigDecimal odds;

    @Enumerated(EnumType.STRING)
    @Column(name = "bet_type", nullable = false)
    private BetType betType;

    @Enumerated(EnumType.STRING)
    @Column(name = "bet_result", nullable = false)
    private BetResult betResult = BetResult.PENDING;

    // 생성
    public static  Betting createBetting(
            Long userId,
            UUID gameId,
            Integer betAmount,
            BigDecimal odds,
            BetType betType,
            BetResult betResult
    ){
        Betting betting = new Betting();
        betting.setUserId(userId);
        betting.setGameId(gameId);
        betting.setBetAmount(betAmount);
        betting.setOdds(odds);
        betting.setBetType(betType);
        betting.setBetResult(betResult);
        return betting;
    }

    // 수정
    public void updateBetting(
            Integer betAmount,
            BigDecimal odds,
            BetType betType,
            BetResult betResult
    ){
        if(betResult != null) this.betAmount = betAmount;
        if(odds != null) this.odds = odds;
        if(betType != null) this.betType = betType;
        if(betResult != null) this.betResult = betResult;
    }
}
