package com.oneonone.bettingservice.domain;

import com.oneonone.common.enums.GameResult;
import com.oneonone.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@Where(clause = "deleted_at is null")
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
    private BigDecimal betAmount;

    @Column(name = "odds", nullable = false)
    private BigDecimal odds;

    @Enumerated(EnumType.STRING)
    @Column(name = "bet_type", nullable = false)
    private GameResult betType;

    @Enumerated(EnumType.STRING)
    @Column(name = "bet_result", nullable = false)
    private BetResult betResult = BetResult.PENDING;

    // 생성
    public static  Betting createBetting(
            Long userId,
            UUID gameId,
            BigDecimal betAmount,
            BigDecimal odds,
            GameResult betType
    ){
        Betting betting = new Betting();
        betting.setUserId(userId);
        betting.setGameId(gameId);
        betting.setBetAmount(betAmount);
        betting.setOdds(odds);
        betting.setBetType(betType);
        return betting;
    }

    // 수정
    public void updateBetting(
            BigDecimal betAmount,
            BigDecimal odds,
            GameResult betType
    ){
        if(betResult != null) this.betAmount = betAmount;
        if(odds != null) this.odds = odds;
        if(betType != null) this.betType = betType;
    }

    // 경기결과 업데이트
    public void updateResult(
            BetResult betResult
    ){
        if(betResult != null) this.betResult = betResult;
    }

    // 승리 여부
    public boolean isWin() {
        return this.betResult == BetResult.WIN;
    }

    // 정산
    public Long calculateReward(){
        return betAmount
                .multiply(odds)
                .longValue();
    }
}
