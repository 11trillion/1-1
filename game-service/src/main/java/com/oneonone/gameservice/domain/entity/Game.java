package com.oneonone.gameservice.domain.entity;

import com.oneonone.common.enums.GameResult;
import com.oneonone.common.exception.BusinessException;
import com.oneonone.common.model.BaseEntity;
import com.oneonone.gameservice.domain.GameErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_games")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Game extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "game_id", nullable = false, updatable = false)
    private UUID gameId;

    @Column(name = "home_team", nullable = false )
    private String homeTeam;

    @Column(name = "away_team", nullable = false)
    private String awayTeam;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;
    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "home_score")
    private int homeScore;

    @Column(name = "away_score")
    private int awayScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private GameStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "result",nullable = false)
    private GameResult result;

    public static Game createGame(String homeTeam, String awayTeam, LocalDateTime startAt) {
        //팀 확인
        validateTeams(homeTeam, awayTeam);
        if (startAt == null) {
            throw new BusinessException(GameErrorCode.START_TIME_ERROR);
        }

        return Game.builder()
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .startAt(startAt)
                .endAt(null)
                .homeScore(0)
                .awayScore(0)
                .status(GameStatus.SCHEDULED)
                .result(GameResult.WAIT)
                .build();
    }

    private static void validateTeams(String homeTeam, String awayTeam) {
        if (homeTeam == null || homeTeam.isBlank()) {
            throw new BusinessException(GameErrorCode.HOME_TEAM_ERROR);
        }
        if (awayTeam == null || awayTeam.isBlank()) {
            throw new BusinessException(GameErrorCode.AWAY_TEAM_ERROR);
        }
        if (homeTeam.trim().equalsIgnoreCase(awayTeam.trim())) {
            throw new BusinessException(GameErrorCode.TEAM_DUPLICATED_ERROR);
        }
    }

    private static void validateScore(int homeScore, int awayScore) {
        if (homeScore < 0 || awayScore < 0) {
            throw new BusinessException(GameErrorCode.SCORE_ERROR);
        }
    }

    private static void validateTime(LocalDateTime startAt, LocalDateTime endAt) {
        if (endAt != null && endAt.isBefore(startAt)) {
            throw new BusinessException(GameErrorCode.END_TIME_ERROR);
        }
    }

    /**
    public void start() {
        if(!status.isScheduled()) {
            throw new BusinessException(GameErrorCode.GAME_START_ERROR);
        }
        this.status = GameStatus.PROGRESS;
        this.result = GameResult.WAIT; //경기 진행 중
    }

    public void end (LocalDateTime endAt) {
        if(!status.isProgress()) {
            throw new BusinessException(GameErrorCode.GAME_END_ERROR);
        }
        validateTime(this.startAt, endAt);
        this.endAt = endAt;
        this.status = GameStatus.END;
        this.result = GameResult.checkScore(homeScore, awayScore);
    }
**/
    public void update(
            String homeTeam, String awayTeam, LocalDateTime startAt,
            LocalDateTime endAt, Integer homeScore, Integer awayScore, GameStatus status) {

        //상태 END면 수정 불가능 end 로직에서 위로 당겨옴
        if (this.status.isEnded()) {
            throw new BusinessException(GameErrorCode.GAME_ALREADY_ENDED);
        }

        //PatchMapping으로 null 값을 비교한다. null이면 기존값을 사용한다.
        String newHomeTeam      = (homeTeam   != null && !homeTeam.isBlank()) ? homeTeam   : this.homeTeam;
        String newAwayTeam      = (awayTeam   != null && !awayTeam.isBlank()) ? awayTeam   : this.awayTeam;
        LocalDateTime newStartAt = (startAt   != null ) ? startAt    : this.startAt;
        LocalDateTime newEndAt   = (endAt     != null) ? endAt      : this.endAt;
        int newHomeScore        = (homeScore  != null) ? homeScore  : this.homeScore;
        int newAwayScore        = (awayScore  != null) ? awayScore  : this.awayScore;
        GameStatus newStatus    = status;


        //patch 도입으로 바뀐 값을 검증함.
        validateTeams(newHomeTeam, newAwayTeam);

        //상태별 처리(Patch 적용 ver)

        if (status == GameStatus.SCHEDULED) {
            this.homeTeam = newHomeTeam;
            this.awayTeam = newAwayTeam;
            this.startAt = newStartAt;
            this.homeScore = 0;
            this.awayScore = 0;
            this.endAt = null;
            this.status = GameStatus.SCHEDULED;
            this.result = GameResult.WAIT;
            return;
        }

        //점수 수정 가능/ 종료시간 x
        if (status == GameStatus.PROGRESS) {
            //점수 검증
            validateScore(newHomeScore, newAwayScore);

            this.homeTeam = newHomeTeam;
            this.awayTeam = newAwayTeam;
            this.startAt = newStartAt;
            this.homeScore = newHomeScore;
            this.awayScore = newAwayScore;
            this.endAt = null;
            this.status = GameStatus.PROGRESS;
            this.result = GameResult.WAIT;
            return;
        }

        if (status == GameStatus.END) {
            if (newEndAt == null) {
                throw new BusinessException(GameErrorCode.ENDED_GAME_TIME_ERROR);
            }
            validateScore(newHomeScore, newAwayScore);
            validateTime(newStartAt, newEndAt);
            this.homeTeam = newHomeTeam;
            this.awayTeam = newAwayTeam;
            this.startAt = newStartAt;
            this.endAt = newEndAt;
            this.homeScore = newHomeScore;
            this.awayScore = newAwayScore;
            this.status = GameStatus.END;
            this.result = GameResult.checkScore(newHomeScore, newAwayScore);
            return;
        }
        throw new BusinessException(GameErrorCode.INVALID_STATUS);
    }


}
