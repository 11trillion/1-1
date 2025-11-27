package com.oneonone.bettingservice.domain;

import jakarta.persistence.*;
import jakarta.validation.groups.Default;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "p_betting")
public class Betting {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "bet_id")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "game_id", nullable = false)
    private UUID gameId;

    @Column(name = "bet_amount", nullable = false)
    private Integer betAmount;

    @Column(name = "oods", nullable = false)
    private BigDecimal odds;

    @Enumerated(EnumType.STRING)
    @Column(name = "bet_type", nullable = false)
    private BetType betType;

    @Enumerated(EnumType.STRING)
    @Column(name = "bet_result", nullable = false)
    private BetResult betResult = BetResult.PENDING;




}
