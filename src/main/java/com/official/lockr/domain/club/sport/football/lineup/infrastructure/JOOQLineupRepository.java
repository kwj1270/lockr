package com.official.lockr.domain.club.sport.football.lineup.infrastructure;

import com.official.lockr.domain.club.sport.football.lineup.domain.Lineup;
import com.official.lockr.domain.club.sport.football.lineup.domain.LineupRepository;
import com.official.lockr.domain.club.sport.football.lineup.domain.LineupSlot;
import com.official.lockr.domain.club.sport.football.lineup.domain.LineupSlots;
import com.official.lockr.domain.club.sport.football.lineup.domain.vo.SlotType;
import com.official.lockr.global.ddd.DomainEventPublisher;
import com.official.lockr.global.vo.Formation;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.LineupSlotsDao;
import org.jooq.generated.tables.daos.LineupsDao;
import org.jooq.generated.tables.pojos.LineupSlotsEntity;
import org.jooq.generated.tables.pojos.LineupsEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.jooq.generated.tables.LineupSlotsJOOQEntity.LINEUP_SLOTS;
import static org.jooq.generated.tables.LineupsJOOQEntity.LINEUPS;
import static org.jooq.impl.DSL.excluded;

@Component
class JOOQLineupRepository implements LineupRepository {

    private final LineupsDao lineupsDao;
    private final LineupSlotsDao lineupSlotsDao;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQLineupRepository(final Configuration configuration,
                                final DomainEventPublisher domainEventPublisher) {
        this.lineupsDao = new LineupsDao(configuration);
        this.lineupSlotsDao = new LineupSlotsDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Nullable
    @Override
    public Lineup findById(final String id) {
        final LineupsEntity lineupEntity = lineupsDao.ctx()
                .selectFrom(LINEUPS)
                .where(LINEUPS.ID.eq(id))
                .fetchOptional()
                .map(record -> new LineupsEntity(
                        record.getId(),
                        record.getClubId(),
                        record.getName(),
                        record.getFormation(),
                        record.getCreatedAt(),
                        record.getUpdatedAt(),
                        record.getDeletedAt()
                ))
                .orElse(null);
        if (isNull(lineupEntity)) {
            return null;
        }
        return toDomain(lineupEntity, findPlayersByLineupId(lineupEntity.getId()));
    }

    @Override
    public List<Lineup> findByClubId(final String clubId) {
        final List<LineupsEntity> lineupEntities = lineupsDao.ctx()
                .selectFrom(LINEUPS)
                .where(LINEUPS.CLUB_ID.eq(clubId))
                .fetchInto(LineupsEntity.class);

        return lineupEntities.stream()
                .map(entity -> toDomain(entity, findPlayersByLineupId(entity.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public Lineup save(final Lineup lineup) {
        upsertLineup(lineup);
        syncPlayers(lineup);
        lineup.publish(domainEventPublisher);
        return lineup;
    }

    @Transactional
    @Override
    public List<Lineup> saveAll(final List<Lineup> lineups) {
        for (final Lineup lineup : lineups) {
            upsertLineup(lineup);
            syncPlayers(lineup);
            lineup.publish(domainEventPublisher);
        }
        return lineups;
    }

    private void upsertLineup(final Lineup lineup) {
        lineupsDao.ctx()
                .insertInto(LINEUPS)
                .set(LINEUPS.ID, lineup.getId())
                .set(LINEUPS.CLUB_ID, lineup.getClubId())
                .set(LINEUPS.NAME, lineup.getName())
                .set(LINEUPS.FORMATION, lineup.getFormation().getName())
                .set(LINEUPS.CREATED_AT, lineup.getCreatedAt())
                .set(LINEUPS.UPDATED_AT, lineup.getUpdatedAt())
                .set(LINEUPS.DELETED_AT, lineup.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(LINEUPS.NAME, excluded(LINEUPS.NAME))
                .set(LINEUPS.FORMATION, excluded(LINEUPS.FORMATION))
                .set(LINEUPS.UPDATED_AT, excluded(LINEUPS.UPDATED_AT))
                .set(LINEUPS.DELETED_AT, excluded(LINEUPS.DELETED_AT))
                .execute();
    }

    private void syncPlayers(final Lineup lineup) {
        final List<String> existingPlayerIds = lineupSlotsDao.ctx()
                .select(LINEUP_SLOTS.ID)
                .from(LINEUP_SLOTS)
                .where(LINEUP_SLOTS.LINEUP_ID.eq(lineup.getId()))
                .fetchInto(String.class);

        final List<String> currentPlayerIds = lineup.getLineupPlayers().stream()
                .map(LineupSlot::getId)
                .toList();

        final List<String> playersToDelete = existingPlayerIds.stream()
                .filter(id -> !currentPlayerIds.contains(id))
                .toList();

        if (!playersToDelete.isEmpty()) {
            lineupSlotsDao.ctx()
                    .deleteFrom(LINEUP_SLOTS)
                    .where(LINEUP_SLOTS.ID.in(playersToDelete))
                    .execute();
        }

        if (!lineup.getLineupPlayers().isEmpty()) {
            upsertPlayers(lineup.getLineupPlayers());
        }
    }

    private void upsertPlayers(final List<LineupSlot> lineupSlots) {
        if (lineupSlots.isEmpty()) {
            return;
        }

        var query = lineupSlotsDao.ctx().insertInto(LINEUP_SLOTS,
                LINEUP_SLOTS.ID,
                LINEUP_SLOTS.LINEUP_ID,
                LINEUP_SLOTS.SQUAD_PLAYER_ID,
                LINEUP_SLOTS.SLOT_TYPE,
                LINEUP_SLOTS.SLOT_INDEX,
                LINEUP_SLOTS.CREATED_AT
        );

        for (final LineupSlot player : lineupSlots) {
            query.values(
                    player.getId(),
                    player.getLineupId(),
                    player.getSquadPlayerId(),
                    player.getSlotType().getValue(),
                    player.getSlotIndex(),
                    player.getCreatedAt()
            );
        }

        query.onDuplicateKeyUpdate()
                .set(LINEUP_SLOTS.SQUAD_PLAYER_ID, excluded(LINEUP_SLOTS.SQUAD_PLAYER_ID))
                .set(LINEUP_SLOTS.SLOT_TYPE, excluded(LINEUP_SLOTS.SLOT_TYPE))
                .set(LINEUP_SLOTS.SLOT_INDEX, excluded(LINEUP_SLOTS.SLOT_INDEX))
                .execute();
    }

    private List<LineupSlot> findPlayersByLineupId(final String lineupId) {
        return lineupSlotsDao.ctx()
                .selectFrom(LINEUP_SLOTS)
                .where(LINEUP_SLOTS.LINEUP_ID.eq(lineupId))
                .fetchInto(LineupSlotsEntity.class)
                .stream()
                .map(JOOQLineupRepository::toPlayerDomain)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static Lineup toDomain(final LineupsEntity entity, final List<LineupSlot> lineupSlots) {
        return new Lineup(
                entity.getId(),
                entity.getClubId(),
                entity.getName(),
                Formation.of(entity.getFormation()),
                new LineupSlots(lineupSlots),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private static LineupSlot toPlayerDomain(final LineupSlotsEntity entity) {
        return new LineupSlot(
                entity.getId(),
                entity.getLineupId(),
                entity.getSquadPlayerId(),
                SlotType.fromValue(entity.getSlotType()),
                entity.getSlotIndex(),
                entity.getCreatedAt()
        );
    }
}
