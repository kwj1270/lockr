package com.official.lockr.domain.club.stats.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.stats.api.dto.ComparePlayersRequest;
import com.official.lockr.domain.club.stats.api.dto.MatchRecordResponse;
import com.official.lockr.domain.club.stats.api.dto.MatchRecordsResponse;
import com.official.lockr.domain.club.stats.api.dto.PlayerComparisonResponse;
import com.official.lockr.domain.club.stats.api.dto.PlayerStatsResponse;
import com.official.lockr.domain.club.stats.api.dto.RankingsResponse;
import com.official.lockr.domain.club.stats.api.dto.SeasonStatsResponse;
import com.official.lockr.domain.club.stats.domain.MatchResult;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.generated.tables.daos.MatchRecordsDao;
import org.jooq.generated.tables.daos.PlayerMatchStatsDao;
import org.jooq.impl.DSL;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.jooq.generated.tables.MatchRecordsJOOQEntity.MATCH_RECORDS;
import static org.jooq.generated.tables.PlayerMatchStatsJOOQEntity.PLAYER_MATCH_STATS;
import static org.jooq.generated.tables.UserAdditionalInfoJOOQEntity.USER_ADDITIONAL_INFO;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.sum;

@RequestMapping("/api/v1/clubs/{clubId}/stats")
@RestController
public class StatsQueryApi {

    private final MatchRecordsDao matchRecordsDao;
    private final PlayerMatchStatsDao playerMatchStatsDao;

    public StatsQueryApi(final Configuration configuration) {
        this.matchRecordsDao = new MatchRecordsDao(configuration);
        this.playerMatchStatsDao = new PlayerMatchStatsDao(configuration);
    }

    // ── Task 4: GET /season ──

    @GetMapping("/season")
    public ResponseEntity<SeasonStatsResponse> getSeasonStats(
            @PathVariable final String clubId,
            @RequestParam(required = false) final String season
    ) {
        final var ctx = matchRecordsDao.ctx();

        var condition = MATCH_RECORDS.CLUB_ID.eq(clubId)
                .and(MATCH_RECORDS.DELETED_AT.isNull());

        if (season != null && !season.isBlank()) {
            condition = condition.and(MATCH_RECORDS.SEASON.eq(season));
        }

        final var stats = ctx
                .select(
                        count().as("totalMatches"),
                        sum(DSL.when(MATCH_RECORDS.RESULT.eq(MatchResult.WIN.name()), 1).otherwise(0)).as("wins"),
                        sum(DSL.when(MATCH_RECORDS.RESULT.eq(MatchResult.DRAW.name()), 1).otherwise(0)).as("draws"),
                        sum(DSL.when(MATCH_RECORDS.RESULT.eq(MatchResult.LOSE.name()), 1).otherwise(0)).as("losses"),
                        sum(MATCH_RECORDS.OUR_SCORE).as("goalsScored"),
                        sum(MATCH_RECORDS.OPPONENT_SCORE).as("goalsConceded")
                )
                .from(MATCH_RECORDS)
                .where(condition)
                .fetchOne();

        if (stats == null) {
            return ResponseEntity.ok(new SeasonStatsResponse(
                    clubId, season, 0, 0, 0, 0, 0.0, 0, 0, 0, List.of()
            ));
        }

        final int totalMatches = stats.value1();
        final int wins = stats.value2() != null ? stats.value2().intValue() : 0;
        final int draws = stats.value3() != null ? stats.value3().intValue() : 0;
        final int losses = stats.value4() != null ? stats.value4().intValue() : 0;
        final int goalsScored = stats.value5() != null ? stats.value5().intValue() : 0;
        final int goalsConceded = stats.value6() != null ? stats.value6().intValue() : 0;
        final double winRate = totalMatches > 0 ? (double) wins / totalMatches * 100 : 0.0;

        // recentForm: 최근 5경기 결과
        final List<String> recentForm = ctx
                .select(MATCH_RECORDS.RESULT)
                .from(MATCH_RECORDS)
                .where(condition)
                .orderBy(MATCH_RECORDS.MATCH_DATE.desc())
                .limit(5)
                .fetch()
                .stream()
                .map(r -> {
                    final String result = r.value1();
                    return switch (MatchResult.valueOf(result)) {
                        case WIN -> "W";
                        case DRAW -> "D";
                        case LOSE -> "L";
                    };
                })
                .toList();

        return ResponseEntity.ok(new SeasonStatsResponse(
                clubId,
                season,
                totalMatches,
                wins,
                draws,
                losses,
                Math.round(winRate * 10) / 10.0,
                goalsScored,
                goalsConceded,
                goalsScored - goalsConceded,
                recentForm
        ));
    }

    // ── Task 5: GET /rankings ──

    @GetMapping("/rankings")
    public ResponseEntity<RankingsResponse> getRankings(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @RequestParam(required = false) final String season
    ) {
        final String currentUserId = signInSession.userId();
        final var ctx = playerMatchStatsDao.ctx();

        var matchCondition = MATCH_RECORDS.CLUB_ID.eq(clubId)
                .and(MATCH_RECORDS.DELETED_AT.isNull());
        if (season != null && !season.isBlank()) {
            matchCondition = matchCondition.and(MATCH_RECORDS.SEASON.eq(season));
        }

        final var matchIds = ctx
                .select(MATCH_RECORDS.ID)
                .from(MATCH_RECORDS)
                .where(matchCondition)
                .fetchInto(String.class);

        if (matchIds.isEmpty()) {
            return ResponseEntity.ok(new RankingsResponse(
                    List.of(), List.of(), List.of(), null, null, null
            ));
        }

        final List<RankingsResponse.RankedPlayer> goalRankings = fetchRankings(ctx, matchIds, "goals");
        final List<RankingsResponse.RankedPlayer> assistRankings = fetchRankings(ctx, matchIds, "assists");
        final List<RankingsResponse.RankedPlayer> momRankings = fetchMomRankings(ctx, matchIds);

        final RankingsResponse.RankedPlayer myGoalRank = findMyRank(goalRankings, currentUserId);
        final RankingsResponse.RankedPlayer myAssistRank = findMyRank(assistRankings, currentUserId);
        final RankingsResponse.RankedPlayer myMomRank = findMyRank(momRankings, currentUserId);

        return ResponseEntity.ok(new RankingsResponse(
                goalRankings, assistRankings, momRankings,
                myGoalRank, myAssistRank, myMomRank
        ));
    }

    private RankingsResponse.RankedPlayer findMyRank(
            final List<RankingsResponse.RankedPlayer> rankings,
            final String userId
    ) {
        return rankings.stream()
                .filter(r -> r.playerId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    private List<RankingsResponse.RankedPlayer> fetchRankings(
            final DSLContext ctx,
            final List<String> matchIds,
            final String statType
    ) {
        final var field = "goals".equals(statType) ? PLAYER_MATCH_STATS.GOALS : PLAYER_MATCH_STATS.ASSISTS;

        final var records = ctx
                .select(
                        PLAYER_MATCH_STATS.USER_ID,
                        USER_ADDITIONAL_INFO.NAME,
                        sum(field).as("total")
                )
                .from(PLAYER_MATCH_STATS)
                .join(USER_ADDITIONAL_INFO).on(PLAYER_MATCH_STATS.USER_ID.eq(USER_ADDITIONAL_INFO.USER_ID))
                .where(PLAYER_MATCH_STATS.MATCH_RECORD_ID.in(matchIds))
                .groupBy(PLAYER_MATCH_STATS.USER_ID, USER_ADDITIONAL_INFO.NAME)
                .orderBy(DSL.field("total").desc())
                .limit(10)
                .fetch();

        final List<RankingsResponse.RankedPlayer> unranked = records.stream()
                .map(record -> new RankingsResponse.RankedPlayer(
                        record.value1(),
                        record.value2(),
                        record.value3() != null ? ((Number) record.value3()).intValue() : 0,
                        0
                ))
                .toList();

        return assignDenseRanks(unranked);
    }

    private List<RankingsResponse.RankedPlayer> fetchMomRankings(
            final DSLContext ctx,
            final List<String> matchIds
    ) {
        final var records = ctx
                .select(
                        PLAYER_MATCH_STATS.USER_ID,
                        USER_ADDITIONAL_INFO.NAME,
                        count().as("total")
                )
                .from(PLAYER_MATCH_STATS)
                .join(USER_ADDITIONAL_INFO).on(PLAYER_MATCH_STATS.USER_ID.eq(USER_ADDITIONAL_INFO.USER_ID))
                .where(PLAYER_MATCH_STATS.MATCH_RECORD_ID.in(matchIds))
                .and(PLAYER_MATCH_STATS.IS_MOM.eq(true))
                .groupBy(PLAYER_MATCH_STATS.USER_ID, USER_ADDITIONAL_INFO.NAME)
                .orderBy(DSL.field("total").desc())
                .limit(10)
                .fetch();

        final List<RankingsResponse.RankedPlayer> unranked = records.stream()
                .map(record -> new RankingsResponse.RankedPlayer(
                        record.value1(),
                        record.value2(),
                        record.value3(),
                        0
                ))
                .toList();

        return assignDenseRanks(unranked);
    }

    private List<RankingsResponse.RankedPlayer> assignDenseRanks(final List<RankingsResponse.RankedPlayer> players) {
        final List<RankingsResponse.RankedPlayer> ranked = new ArrayList<>();
        int rank = 1;
        for (int i = 0; i < players.size(); i++) {
            if (i > 0 && players.get(i).value() != players.get(i - 1).value()) {
                rank = i + 1;
            }
            ranked.add(new RankingsResponse.RankedPlayer(
                    players.get(i).playerId(),
                    players.get(i).playerName(),
                    players.get(i).value(),
                    rank
            ));
        }
        return ranked;
    }

    // ── Task 3: GET /matches ──

    @GetMapping("/matches")
    public ResponseEntity<MatchRecordsResponse> getMatchRecords(
            @PathVariable final String clubId,
            @RequestParam(required = false) final String season,
            @RequestParam(required = false) final String scheduleId
    ) {
        final var ctx = matchRecordsDao.ctx();

        var condition = MATCH_RECORDS.CLUB_ID.eq(clubId)
                .and(MATCH_RECORDS.DELETED_AT.isNull());

        if (season != null && !season.isBlank()) {
            condition = condition.and(MATCH_RECORDS.SEASON.eq(season));
        }

        if (scheduleId != null && !scheduleId.isBlank()) {
            condition = condition.and(MATCH_RECORDS.SCHEDULE_ID.eq(scheduleId));
        }

        // 1. 매치 레코드 조회
        final var matchRecords = ctx
                .selectFrom(MATCH_RECORDS)
                .where(condition)
                .orderBy(MATCH_RECORDS.MATCH_DATE.desc())
                .fetch();

        if (matchRecords.isEmpty()) {
            return ResponseEntity.ok(new MatchRecordsResponse(List.of()));
        }

        // 2. 반환된 매치 ID 목록으로 player_match_stats 일괄 조회 (playerName JOIN)
        final List<String> matchIds = matchRecords.stream()
                .map(r -> r.getId())
                .toList();

        final Map<String, List<MatchRecordResponse.PlayerPerformanceResponse>> performanceMap =
                ctx.select(
                                PLAYER_MATCH_STATS.ID,
                                PLAYER_MATCH_STATS.MATCH_RECORD_ID,
                                PLAYER_MATCH_STATS.USER_ID,
                                USER_ADDITIONAL_INFO.NAME,
                                PLAYER_MATCH_STATS.GOALS,
                                PLAYER_MATCH_STATS.ASSISTS,
                                PLAYER_MATCH_STATS.IS_MOM,
                                PLAYER_MATCH_STATS.MINUTES_PLAYED
                        )
                        .from(PLAYER_MATCH_STATS)
                        .leftJoin(USER_ADDITIONAL_INFO).on(PLAYER_MATCH_STATS.USER_ID.eq(USER_ADDITIONAL_INFO.USER_ID))
                        .where(PLAYER_MATCH_STATS.MATCH_RECORD_ID.in(matchIds))
                        .fetch()
                        .stream()
                        .map(r -> Map.entry(
                                r.get(PLAYER_MATCH_STATS.MATCH_RECORD_ID),
                                new MatchRecordResponse.PlayerPerformanceResponse(
                                        r.get(PLAYER_MATCH_STATS.ID),
                                        r.get(PLAYER_MATCH_STATS.USER_ID),
                                        r.get(USER_ADDITIONAL_INFO.NAME) != null ? r.get(USER_ADDITIONAL_INFO.NAME) : "",
                                        r.get(PLAYER_MATCH_STATS.GOALS),
                                        r.get(PLAYER_MATCH_STATS.ASSISTS),
                                        r.get(PLAYER_MATCH_STATS.IS_MOM),
                                        r.get(PLAYER_MATCH_STATS.MINUTES_PLAYED)
                                )
                        ))
                        .collect(Collectors.groupingBy(
                                Map.Entry::getKey,
                                Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                        ));

        // 3. 각 MatchRecordResponse 생성 시 map에서 조회
        final var records = matchRecords.stream()
                .map(record -> new MatchRecordResponse(
                        record.getId(),
                        record.getClubId(),
                        record.getScheduleId(),
                        record.getMatchDate(),
                        record.getOpponentName(),
                        record.getOurScore(),
                        record.getOpponentScore(),
                        MatchResult.valueOf(record.getResult()),
                        record.getSeason(),
                        performanceMap.getOrDefault(record.getId(), List.of()),
                        record.getCreatedAt(),
                        record.getRecordedBy()
                ))
                .toList();

        return ResponseEntity.ok(new MatchRecordsResponse(records));
    }

    // ── Task 6: GET /me ──

    @GetMapping("/me")
    public ResponseEntity<PlayerStatsResponse> getMyStats(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @RequestParam(required = false) final String season
    ) {
        final String userId = signInSession.userId();
        return ResponseEntity.ok(buildPlayerStats(clubId, userId, season));
    }

    // ── Task 6: GET /players/{playerId} ──

    @GetMapping("/players/{playerId}")
    public ResponseEntity<PlayerStatsResponse> getPlayerStats(
            @PathVariable final String clubId,
            @PathVariable final String playerId,
            @RequestParam(required = false) final String season
    ) {
        return ResponseEntity.ok(buildPlayerStats(clubId, playerId, season));
    }

    private PlayerStatsResponse buildPlayerStats(
            final String clubId,
            final String playerId,
            final String season
    ) {
        final var ctx = matchRecordsDao.ctx();

        var matchCondition = MATCH_RECORDS.CLUB_ID.eq(clubId)
                .and(MATCH_RECORDS.DELETED_AT.isNull());
        if (season != null && !season.isBlank()) {
            matchCondition = matchCondition.and(MATCH_RECORDS.SEASON.eq(season));
        }

        // playerName 조회
        final String playerName = ctx
                .select(USER_ADDITIONAL_INFO.NAME)
                .from(USER_ADDITIONAL_INFO)
                .where(USER_ADDITIONAL_INFO.USER_ID.eq(playerId))
                .fetchOneInto(String.class);

        // 클럽 전체 매치 수
        final int totalClubMatches = ctx
                .selectCount()
                .from(MATCH_RECORDS)
                .where(matchCondition)
                .fetchOneInto(int.class);

        if (totalClubMatches == 0) {
            return new PlayerStatsResponse(
                    clubId, playerId, playerName != null ? playerName : "",
                    0, 0, 0, 0, 0.0, List.of()
            );
        }

        final var matchIds = ctx
                .select(MATCH_RECORDS.ID)
                .from(MATCH_RECORDS)
                .where(matchCondition)
                .fetchInto(String.class);

        // 선수 집계 통계
        final var stats = playerMatchStatsDao.ctx()
                .select(
                        count().as("appearances"),
                        sum(PLAYER_MATCH_STATS.GOALS).as("totalGoals"),
                        sum(PLAYER_MATCH_STATS.ASSISTS).as("totalAssists"),
                        sum(DSL.when(PLAYER_MATCH_STATS.IS_MOM.eq(true), 1).otherwise(0)).as("momCount")
                )
                .from(PLAYER_MATCH_STATS)
                .where(PLAYER_MATCH_STATS.MATCH_RECORD_ID.in(matchIds))
                .and(PLAYER_MATCH_STATS.USER_ID.eq(playerId))
                .fetchOne();

        final int appearances = stats != null ? stats.value1() : 0;
        final int totalGoals = stats != null && stats.value2() != null ? stats.value2().intValue() : 0;
        final int totalAssists = stats != null && stats.value3() != null ? stats.value3().intValue() : 0;
        final int momCount = stats != null && stats.value4() != null ? stats.value4().intValue() : 0;

        // attendanceRate
        final double attendanceRate = totalClubMatches > 0
                ? Math.round((double) appearances / totalClubMatches * 100 * 10) / 10.0
                : 0.0;

        // recentForm: 클럽 최근 5매치에 대해 LEFT JOIN player_match_stats
        final var recentMatchIds = ctx
                .select(MATCH_RECORDS.ID)
                .from(MATCH_RECORDS)
                .where(matchCondition)
                .orderBy(MATCH_RECORDS.MATCH_DATE.desc())
                .limit(5)
                .fetchInto(String.class);

        final List<String> recentForm;
        if (recentMatchIds.isEmpty()) {
            recentForm = List.of();
        } else {
            // 선수의 최근 매치 참여 데이터
            final Map<String, Record> playerRecentStats = new HashMap<>();
            ctx.select(
                            PLAYER_MATCH_STATS.MATCH_RECORD_ID,
                            PLAYER_MATCH_STATS.GOALS,
                            PLAYER_MATCH_STATS.ASSISTS,
                            PLAYER_MATCH_STATS.IS_MOM
                    )
                    .from(PLAYER_MATCH_STATS)
                    .where(PLAYER_MATCH_STATS.MATCH_RECORD_ID.in(recentMatchIds))
                    .and(PLAYER_MATCH_STATS.USER_ID.eq(playerId))
                    .fetch()
                    .forEach(r -> playerRecentStats.put(
                            r.get(PLAYER_MATCH_STATS.MATCH_RECORD_ID), r
                    ));

            recentForm = new ArrayList<>();
            for (final String matchId : recentMatchIds) {
                final Record playerStat = playerRecentStats.get(matchId);
                if (playerStat == null) {
                    recentForm.add("none");
                } else {
                    final int goals = playerStat.get(PLAYER_MATCH_STATS.GOALS);
                    final int assists = playerStat.get(PLAYER_MATCH_STATS.ASSISTS);
                    final boolean isMom = playerStat.get(PLAYER_MATCH_STATS.IS_MOM);
                    if (goals > 0) {
                        recentForm.add("goal");
                    } else if (assists > 0) {
                        recentForm.add("assist");
                    } else if (isMom) {
                        recentForm.add("mom");
                    } else {
                        recentForm.add("played");
                    }
                }
            }
        }

        return new PlayerStatsResponse(
                clubId, playerId, playerName != null ? playerName : "",
                appearances, totalGoals, totalAssists, momCount,
                attendanceRate, recentForm
        );
    }

    // ── Task 2: POST /compare ──

    @PostMapping("/compare")
    public ResponseEntity<List<PlayerComparisonResponse>> comparePlayers(
            @PathVariable final String clubId,
            @RequestParam(required = false) final String season,
            @RequestBody final ComparePlayersRequest request
    ) {
        final var ctx = matchRecordsDao.ctx();

        var matchCondition = MATCH_RECORDS.CLUB_ID.eq(clubId)
                .and(MATCH_RECORDS.DELETED_AT.isNull());
        if (season != null && !season.isBlank()) {
            matchCondition = matchCondition.and(MATCH_RECORDS.SEASON.eq(season));
        }

        final var matchIds = ctx
                .select(MATCH_RECORDS.ID)
                .from(MATCH_RECORDS)
                .where(matchCondition)
                .fetchInto(String.class);

        // 선수 이름 일괄 조회
        final Map<String, String> playerNameMap = ctx
                .select(USER_ADDITIONAL_INFO.USER_ID, USER_ADDITIONAL_INFO.NAME)
                .from(USER_ADDITIONAL_INFO)
                .where(USER_ADDITIONAL_INFO.USER_ID.in(request.playerIds()))
                .fetchMap(USER_ADDITIONAL_INFO.USER_ID, USER_ADDITIONAL_INFO.NAME);

        if (matchIds.isEmpty()) {
            final List<PlayerComparisonResponse> emptyResults = request.playerIds().stream()
                    .map(pid -> new PlayerComparisonResponse(
                            pid,
                            playerNameMap.getOrDefault(pid, ""),
                            new PlayerComparisonResponse.PlayerComparisonStats(0, 0, 0, 0, 0.0, 0.0, 0.0)
                    ))
                    .toList();
            return ResponseEntity.ok(emptyResults);
        }

        // 클럽 총 득점
        final Integer clubTotalGoals = ctx
                .select(sum(MATCH_RECORDS.OUR_SCORE))
                .from(MATCH_RECORDS)
                .where(matchCondition)
                .fetchOneInto(Integer.class);
        final int totalGoals = clubTotalGoals != null ? clubTotalGoals : 0;

        // 선수별 집계
        final var playerStats = ctx
                .select(
                        PLAYER_MATCH_STATS.USER_ID,
                        USER_ADDITIONAL_INFO.NAME,
                        count().as("appearances"),
                        sum(PLAYER_MATCH_STATS.GOALS).as("goals"),
                        sum(PLAYER_MATCH_STATS.ASSISTS).as("assists"),
                        sum(DSL.when(PLAYER_MATCH_STATS.IS_MOM.eq(true), 1).otherwise(0)).as("momCount")
                )
                .from(PLAYER_MATCH_STATS)
                .join(USER_ADDITIONAL_INFO).on(PLAYER_MATCH_STATS.USER_ID.eq(USER_ADDITIONAL_INFO.USER_ID))
                .where(PLAYER_MATCH_STATS.MATCH_RECORD_ID.in(matchIds))
                .and(PLAYER_MATCH_STATS.USER_ID.in(request.playerIds()))
                .groupBy(PLAYER_MATCH_STATS.USER_ID, USER_ADDITIONAL_INFO.NAME)
                .fetch();

        // map으로 변환
        final Map<String, PlayerComparisonResponse> statsMap = new HashMap<>();
        for (final var row : playerStats) {
            final String userId = row.value1();
            final String userName = row.value2();
            final int appearances = row.value3();
            final int goals = row.value4() != null ? ((Number) row.value4()).intValue() : 0;
            final int assists = row.value5() != null ? ((Number) row.value5()).intValue() : 0;
            final int momCount = row.value6() != null ? ((Number) row.value6()).intValue() : 0;

            final double goalsPerGame = appearances > 0 ? Math.round((double) goals / appearances * 100) / 100.0 : 0.0;
            final double assistsPerGame = appearances > 0 ? Math.round((double) assists / appearances * 100) / 100.0 : 0.0;
            final double contributionRate = totalGoals > 0
                    ? Math.round((double) (goals + assists) / totalGoals * 100 * 10) / 10.0
                    : 0.0;

            statsMap.put(userId, new PlayerComparisonResponse(
                    userId,
                    userName,
                    new PlayerComparisonResponse.PlayerComparisonStats(
                            appearances, goals, assists, momCount,
                            goalsPerGame, assistsPerGame, contributionRate
                    )
            ));
        }

        // 요청된 playerIds 순서 유지, 데이터 없는 선수도 포함
        final List<PlayerComparisonResponse> result = request.playerIds().stream()
                .map(pid -> statsMap.getOrDefault(pid, new PlayerComparisonResponse(
                        pid,
                        playerNameMap.getOrDefault(pid, ""),
                        new PlayerComparisonResponse.PlayerComparisonStats(0, 0, 0, 0, 0.0, 0.0, 0.0)
                )))
                .toList();

        return ResponseEntity.ok(result);
    }
}
