package com.oneonone.gameservice.domain.entity;

import com.oneonone.common.model.BaseEntity;
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
            throw new  IllegalArgumentException("시작 시간 값은 필수입니다.");
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
            throw new IllegalArgumentException("홈 팀은 필수입니다.");
        }
        if (awayTeam == null || awayTeam.isBlank()) {
            throw new IllegalArgumentException("어웨이 팀은 필수입니다.");
        }
        if (homeTeam.trim().equalsIgnoreCase(awayTeam.trim())) {
            throw new IllegalArgumentException("홈/어웨이 팀은 같을 수 없습니다.");
        }
    }

    private static void validateScore(int homeScore, int awayScore) {
        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException("점수는 무조건 0 이상이어야 합니다.");
        }
    }

    private static void validateTime(LocalDateTime startAt, LocalDateTime endAt) {
        if (endAt != null && endAt.isBefore(startAt)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 이전일 수 없습니다.");
        }
    }

    public void start() {
        if(!status.isScheduled()) {
            throw new IllegalArgumentException("대기 중인 경기에만 시작할 수 있습니다.");
        }
        this.status = GameStatus.PROGRESS;
        this.result = GameResult.WAIT; //경기 진행 중
    }

    public void end (LocalDateTime endAt) {
        if(!status.isProgress()) {
            throw new IllegalArgumentException("진행 중인 경기만 종료 가능합니다");
        }
        validateTime(this.startAt, endAt);
        this.endAt = endAt;
        this.status = GameStatus.END;
        this.result = GameResult.checkScore(homeScore, awayScore);
    }

    public void update(
            String homeTeam, String awayTeam, LocalDateTime startAt,
            LocalDateTime endAt, int homeScore, int awayScore, GameStatus status) {

        validateTeams(homeTeam, awayTeam);
        validateScore(homeScore, awayScore);


        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.startAt = startAt;
        this.homeScore = homeScore;
        this.awayScore = awayScore;

        if (status == GameStatus.SCHEDULED) {
            this.endAt = null;
            this.status = GameStatus.SCHEDULED;
            this.result = GameResult.WAIT;
            return;
        }

        if (status == GameStatus.PROGRESS) {
            this.endAt = null;
            this.status = GameStatus.PROGRESS;
            this.result = GameResult.WAIT;
            return;
        }

        if (status == GameStatus.END) {
            if (endAt == null) {
                throw new IllegalArgumentException("종료 상태에서는 종료 시간이 필수입니다.");
            }
            validateTime(startAt, endAt);
            this.endAt = endAt;
            this.status = GameStatus.END;
            this.result = GameResult.checkScore(homeScore, awayScore);
            return;
        }

        // 나머지 상태는 기본 적용
        this.status = status;
        this.result = GameResult.checkScore(homeScore, awayScore);



    }


}
