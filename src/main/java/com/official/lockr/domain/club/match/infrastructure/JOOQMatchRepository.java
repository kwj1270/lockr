package com.official.lockr.domain.club.match.infrastructure;

import com.official.lockr.domain.club.match.domain.Match;
import com.official.lockr.domain.club.match.domain.MatchRepository;
import com.official.lockr.domain.club.match.domain.MatchStatus;
import com.official.lockr.global.ddd.DomainEventPublisher;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.MatchesDao;
import org.jooq.generated.tables.pojos.MatchesEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.jooq.generated.tables.MatchesJOOQEntity.MATCHES;

@Component
public class JOOQMatchRepository implements MatchRepository {

    private final MatchesDao matchesDao;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQMatchRepository(final Configuration configuration,
                               final DomainEventPublisher domainEventPublisher) {
        this.matchesDao = new MatchesDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Transactional
    @Override
    public Match save(final Match match) {
        upsertMatch(match);
        match.publish(domainEventPublisher);
        return match;
    }

    @Nullable
    @Override
    public Match find(final String id) {
        return matchesDao.ctx()
                .selectFrom(MATCHES)
                .where(MATCHES.ID.eq(id))
                .fetchOptional()
                .map(record -> new MatchesEntity(
                        record.getId(),
                        record.getHomeClubId(),
                        record.getHomeClubManagerUserId(),
                        record.getAwayClubId(),
                        record.getAwayClubManagerUserId(),
                        record.getLocation(),
                        record.getStatus(),
                        record.getMatchDateTime(),
                        record.getCreatedAt(),
                        record.getUpdatedAt(),
                        record.getDeletedAt()
                ))
                .map(JOOQMatchRepository::domain)
                .orElse(null);
    }

    private void upsertMatch(final Match match) {
        matchesDao.ctx()
                .insertInto(MATCHES)
                .set(MATCHES.ID, match.getId())
                .set(MATCHES.HOME_CLUB_ID, match.getHomeClubId())
                .set(MATCHES.AWAY_CLUB_ID, match.getAwayClubId())
                .set(MATCHES.MATCH_DATE_TIME, match.getMatchDateTime())
                .set(MATCHES.LOCATION, match.getLocation())
                .set(MATCHES.STATUS, match.getStatus().name())
                .set(MATCHES.CREATED_AT, match.getCreatedAt())
                .set(MATCHES.UPDATED_AT, match.getUpdatedAt())
                .set(MATCHES.DELETED_AT, match.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(MATCHES.STATUS, match.getStatus().name())
                .set(MATCHES.UPDATED_AT, match.getUpdatedAt())
                .set(MATCHES.DELETED_AT, match.getDeletedAt())
                .execute();
    }

    private static Match domain(final MatchesEntity entity) {
        return new Match(
                entity.getId(),
                entity.getHomeClubId(),
                entity.getHomeClubManagerUserId(),
                entity.getAwayClubId(),
                entity.getAwayClubManagerUserId(),
                entity.getLocation(),
                MatchStatus.valueOf(entity.getStatus()),
                entity.getMatchDateTime(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}
