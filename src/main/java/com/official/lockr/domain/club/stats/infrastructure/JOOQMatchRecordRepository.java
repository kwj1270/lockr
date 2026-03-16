package com.official.lockr.domain.club.stats.infrastructure;

import com.official.lockr.domain.club.stats.domain.MatchRecord;
import com.official.lockr.domain.club.stats.domain.MatchRecordRepository;
import com.official.lockr.domain.club.stats.domain.MatchResult;
import com.official.lockr.domain.club.stats.domain.MatchScore;
import com.official.lockr.domain.club.stats.domain.PlayerPerformance;
import com.official.lockr.global.ddd.DomainEventPublisher;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.MatchRecordsDao;
import org.jooq.generated.tables.daos.PlayerMatchStatsDao;
import org.jooq.generated.tables.pojos.MatchRecordsEntity;
import org.jooq.generated.tables.pojos.PlayerMatchStatsEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.jooq.generated.tables.MatchRecordsJOOQEntity.MATCH_RECORDS;
import static org.jooq.generated.tables.PlayerMatchStatsJOOQEntity.PLAYER_MATCH_STATS;

@Repository
public class JOOQMatchRecordRepository implements MatchRecordRepository {

    private final MatchRecordsDao matchRecordsDao;
    private final PlayerMatchStatsDao playerMatchStatsDao;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQMatchRecordRepository(final Configuration configuration,
                                     final DomainEventPublisher domainEventPublisher) {
        this.matchRecordsDao = new MatchRecordsDao(configuration);
        this.playerMatchStatsDao = new PlayerMatchStatsDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Transactional
    @Override
    public MatchRecord save(final MatchRecord matchRecord) {
        upsertMatchRecord(matchRecord);
        syncPlayerPerformances(matchRecord);
        matchRecord.publish(domainEventPublisher);
        return matchRecord;
    }

    private void upsertMatchRecord(final MatchRecord matchRecord) {
        matchRecordsDao.ctx()
                .insertInto(MATCH_RECORDS)
                .set(MATCH_RECORDS.ID, matchRecord.getId())
                .set(MATCH_RECORDS.CLUB_ID, matchRecord.getClubId())
                .set(MATCH_RECORDS.SCHEDULE_ID, matchRecord.getScheduleId())
                .set(MATCH_RECORDS.MATCH_DATE, matchRecord.getMatchDate())
                .set(MATCH_RECORDS.OPPONENT_NAME, matchRecord.getOpponentName())
                .set(MATCH_RECORDS.OUR_SCORE, matchRecord.getScore().ourScore())
                .set(MATCH_RECORDS.OPPONENT_SCORE, matchRecord.getScore().opponentScore())
                .set(MATCH_RECORDS.RESULT, matchRecord.getResult().name())
                .set(MATCH_RECORDS.RECORDED_BY, matchRecord.getRecordedBy())
                .set(MATCH_RECORDS.SEASON, matchRecord.getSeason())
                .set(MATCH_RECORDS.CREATED_AT, matchRecord.getCreatedAt())
                .set(MATCH_RECORDS.UPDATED_AT, matchRecord.getUpdatedAt())
                .set(MATCH_RECORDS.DELETED_AT, matchRecord.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(MATCH_RECORDS.SCHEDULE_ID, matchRecord.getScheduleId())
                .set(MATCH_RECORDS.MATCH_DATE, matchRecord.getMatchDate())
                .set(MATCH_RECORDS.OPPONENT_NAME, matchRecord.getOpponentName())
                .set(MATCH_RECORDS.OUR_SCORE, matchRecord.getScore().ourScore())
                .set(MATCH_RECORDS.OPPONENT_SCORE, matchRecord.getScore().opponentScore())
                .set(MATCH_RECORDS.RESULT, matchRecord.getResult().name())
                .set(MATCH_RECORDS.UPDATED_AT, matchRecord.getUpdatedAt())
                .set(MATCH_RECORDS.DELETED_AT, matchRecord.getDeletedAt())
                .execute();
    }

    private void syncPlayerPerformances(final MatchRecord matchRecord) {
        final List<String> existingIds = playerMatchStatsDao.ctx()
                .select(PLAYER_MATCH_STATS.ID)
                .from(PLAYER_MATCH_STATS)
                .where(PLAYER_MATCH_STATS.MATCH_RECORD_ID.eq(matchRecord.getId()))
                .fetchInto(String.class);

        final List<String> currentIds = matchRecord.getPlayerPerformances().stream()
                .map(PlayerPerformance::getId)
                .toList();

        final List<String> toDelete = existingIds.stream()
                .filter(id -> !currentIds.contains(id))
                .toList();

        if (!toDelete.isEmpty()) {
            playerMatchStatsDao.ctx()
                    .deleteFrom(PLAYER_MATCH_STATS)
                    .where(PLAYER_MATCH_STATS.ID.in(toDelete))
                    .execute();
        }

        if (!matchRecord.getPlayerPerformances().isEmpty()) {
            upsertPlayerPerformances(matchRecord.getPlayerPerformances());
        }
    }

    private void upsertPlayerPerformances(final List<PlayerPerformance> performances) {
        if (performances.isEmpty()) {
            return;
        }

        var query = playerMatchStatsDao.ctx().insertInto(PLAYER_MATCH_STATS,
                PLAYER_MATCH_STATS.ID,
                PLAYER_MATCH_STATS.MATCH_RECORD_ID,
                PLAYER_MATCH_STATS.CLUB_ID,
                PLAYER_MATCH_STATS.USER_ID,
                PLAYER_MATCH_STATS.GOALS,
                PLAYER_MATCH_STATS.ASSISTS,
                PLAYER_MATCH_STATS.IS_MOM,
                PLAYER_MATCH_STATS.MINUTES_PLAYED
        );

        for (final PlayerPerformance perf : performances) {
            query.values(
                    perf.getId(),
                    perf.getMatchRecordId(),
                    perf.getClubId(),
                    perf.getUserId(),
                    perf.getGoals(),
                    perf.getAssists(),
                    perf.isMom(),
                    perf.getMinutesPlayed()
            );
        }

        query.onDuplicateKeyUpdate()
                .set(PLAYER_MATCH_STATS.GOALS, org.jooq.impl.DSL.excluded(PLAYER_MATCH_STATS.GOALS))
                .set(PLAYER_MATCH_STATS.ASSISTS, org.jooq.impl.DSL.excluded(PLAYER_MATCH_STATS.ASSISTS))
                .set(PLAYER_MATCH_STATS.IS_MOM, org.jooq.impl.DSL.excluded(PLAYER_MATCH_STATS.IS_MOM))
                .set(PLAYER_MATCH_STATS.MINUTES_PLAYED, org.jooq.impl.DSL.excluded(PLAYER_MATCH_STATS.MINUTES_PLAYED))
                .execute();
    }

    @Nullable
    @Override
    public MatchRecord findById(final String id) {
        final MatchRecordsEntity entity = matchRecordsDao.ctx()
                .selectFrom(MATCH_RECORDS)
                .where(MATCH_RECORDS.ID.eq(id))
                .and(MATCH_RECORDS.DELETED_AT.isNull())
                .fetchOneInto(MatchRecordsEntity.class);
        if (Objects.isNull(entity)) {
            return null;
        }
        return domain(entity, findPlayerPerformances(id));
    }

    @Override
    public List<MatchRecord> findAllByClubId(final String clubId) {
        final List<MatchRecordsEntity> entities = matchRecordsDao.ctx()
                .selectFrom(MATCH_RECORDS)
                .where(MATCH_RECORDS.CLUB_ID.eq(clubId))
                .and(MATCH_RECORDS.DELETED_AT.isNull())
                .orderBy(MATCH_RECORDS.MATCH_DATE.desc())
                .fetchInto(MatchRecordsEntity.class);

        return toDomainList(entities);
    }

    @Override
    public List<MatchRecord> findAllByClubIdAndSeason(final String clubId, final String season) {
        final List<MatchRecordsEntity> entities = matchRecordsDao.ctx()
                .selectFrom(MATCH_RECORDS)
                .where(MATCH_RECORDS.CLUB_ID.eq(clubId))
                .and(MATCH_RECORDS.SEASON.eq(season))
                .and(MATCH_RECORDS.DELETED_AT.isNull())
                .orderBy(MATCH_RECORDS.MATCH_DATE.desc())
                .fetchInto(MatchRecordsEntity.class);

        return toDomainList(entities);
    }

    @Nullable
    @Override
    public MatchRecord findByScheduleId(final String scheduleId) {
        final MatchRecordsEntity entity = matchRecordsDao.ctx()
                .selectFrom(MATCH_RECORDS)
                .where(MATCH_RECORDS.SCHEDULE_ID.eq(scheduleId))
                .and(MATCH_RECORDS.DELETED_AT.isNull())
                .fetchOneInto(MatchRecordsEntity.class);
        if (Objects.isNull(entity)) {
            return null;
        }
        return domain(entity, findPlayerPerformances(entity.getId()));
    }

    @Transactional
    @Override
    public void delete(final MatchRecord matchRecord) {
        final MatchRecord deleted = matchRecord.delete();
        upsertMatchRecord(deleted);
        deleted.publish(domainEventPublisher);
    }

    private List<MatchRecord> toDomainList(final List<MatchRecordsEntity> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }

        final List<String> matchRecordIds = entities.stream()
                .map(MatchRecordsEntity::getId)
                .toList();

        final Map<String, List<PlayerPerformance>> performanceMap = findPlayerPerformancesByMatchRecordIds(matchRecordIds);

        return entities.stream()
                .map(entity -> domain(entity, performanceMap.getOrDefault(entity.getId(), List.of())))
                .toList();
    }

    private Map<String, List<PlayerPerformance>> findPlayerPerformancesByMatchRecordIds(final List<String> matchRecordIds) {
        return playerMatchStatsDao.ctx()
                .selectFrom(PLAYER_MATCH_STATS)
                .where(PLAYER_MATCH_STATS.MATCH_RECORD_ID.in(matchRecordIds))
                .fetchInto(PlayerMatchStatsEntity.class)
                .stream()
                .collect(Collectors.groupingBy(
                        PlayerMatchStatsEntity::getMatchRecordId,
                        Collectors.mapping(
                                JOOQMatchRecordRepository::domain,
                                Collectors.toCollection(ArrayList::new)
                        )
                ));
    }

    private List<PlayerPerformance> findPlayerPerformances(final String matchRecordId) {
        return playerMatchStatsDao.ctx()
                .selectFrom(PLAYER_MATCH_STATS)
                .where(PLAYER_MATCH_STATS.MATCH_RECORD_ID.eq(matchRecordId))
                .fetchInto(PlayerMatchStatsEntity.class)
                .stream()
                .map(JOOQMatchRecordRepository::domain)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static MatchRecord domain(final MatchRecordsEntity entity, final List<PlayerPerformance> performances) {
        return new MatchRecord(
                entity.getId(),
                entity.getClubId(),
                entity.getScheduleId(),
                entity.getMatchDate(),
                entity.getOpponentName(),
                new MatchScore(entity.getOurScore(), entity.getOpponentScore()),
                MatchResult.valueOf(entity.getResult()),
                entity.getRecordedBy(),
                entity.getSeason(),
                performances,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private static PlayerPerformance domain(final PlayerMatchStatsEntity entity) {
        return new PlayerPerformance(
                entity.getId(),
                entity.getMatchRecordId(),
                entity.getClubId(),
                entity.getUserId(),
                entity.getGoals(),
                entity.getAssists(),
                entity.getIsMom(),
                entity.getMinutesPlayed()
        );
    }
}
