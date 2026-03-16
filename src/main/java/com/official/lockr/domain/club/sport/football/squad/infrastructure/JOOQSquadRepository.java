package com.official.lockr.domain.club.sport.football.squad.infrastructure;

import com.official.lockr.global.vo.BirthDate;
import com.official.lockr.global.vo.Position;
import com.official.lockr.domain.club.sport.football.squad.domain.Squad;
import com.official.lockr.domain.club.sport.football.squad.domain.SquadPlayer;
import com.official.lockr.domain.club.sport.football.squad.domain.SquadRepository;
import com.official.lockr.global.ddd.DomainEventPublisher;
import com.official.lockr.global.vo.BackNumber;
import com.official.lockr.global.vo.Foot;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.SquadPlayersDao;
import org.jooq.generated.tables.daos.SquadsDao;
import org.jooq.generated.tables.pojos.SquadPlayersEntity;
import org.jooq.generated.tables.pojos.SquadsEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.jooq.generated.tables.SquadPlayersJOOQEntity.SQUAD_PLAYERS;
import static org.jooq.generated.tables.SquadsJOOQEntity.SQUADS;
import static org.jooq.impl.DSL.excluded;

@Repository
public class JOOQSquadRepository implements SquadRepository {

    private final SquadsDao squadsDao;
    private final SquadPlayersDao squadPlayersDao;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQSquadRepository(final Configuration configuration,
                               final DomainEventPublisher domainEventPublisher) {
        this.squadsDao = new SquadsDao(configuration);
        this.squadPlayersDao = new SquadPlayersDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public Squad findById(final String squadId) {
        final SquadsEntity squadEntity = squadsDao.ctx()
                .selectFrom(SQUADS)
                .where(SQUADS.ID.eq(squadId))
                .fetchOptional()
                .map(record -> new SquadsEntity(
                        record.getId(),
                        record.getClubId(),
                        record.getCreatedAt(),
                        record.getUpdatedAt(),
                        record.getDeletedAt()
                ))
                .orElse(null);
        if (isNull(squadEntity)) {
            return null;
        }
        return domain(squadEntity, findPlayersBySquadId(squadEntity.getId()));
    }

    @Nullable
    @Override
    public Squad findByClubId(final String clubId) {
        final SquadsEntity squadEntity = squadsDao.ctx()
                .selectFrom(SQUADS)
                .where(SQUADS.CLUB_ID.eq(clubId))
                .fetchOptional()
                .map(record -> new SquadsEntity(
                        record.getId(),
                        record.getClubId(),
                        record.getCreatedAt(),
                        record.getUpdatedAt(),
                        record.getDeletedAt()
                ))
                .orElse(null);
        if (isNull(squadEntity)) {
            return null;
        }
        return domain(squadEntity, findPlayersBySquadId(squadEntity.getId()));
    }

    @Transactional
    @Override
    public Squad save(final Squad squad) {
        upsertSquad(squad);
        syncPlayers(squad);
        squad.publish(domainEventPublisher);
        return squad;
    }

    private void upsertSquad(final Squad lineUp) {
        squadsDao.ctx()
                .insertInto(SQUADS)
                .set(SQUADS.ID, lineUp.getId())
                .set(SQUADS.CLUB_ID, lineUp.getClubId())
                .set(SQUADS.CREATED_AT, lineUp.getCreatedAt())
                .set(SQUADS.UPDATED_AT, lineUp.getUpdatedAt())
                .set(SQUADS.DELETED_AT, lineUp.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(SQUADS.UPDATED_AT, lineUp.getUpdatedAt())
                .set(SQUADS.DELETED_AT, lineUp.getDeletedAt())
                .execute();
    }

    private void syncPlayers(final Squad lineUp) {
        // 1. 기존 플레이어 조회
        final List<String> existingPlayerIds = squadPlayersDao.ctx()
                .select(SQUAD_PLAYERS.ID)
                .from(SQUAD_PLAYERS)
                .where(SQUAD_PLAYERS.SQUAD_ID.eq(lineUp.getId()))
                .fetchInto(String.class);

        final List<String> currentPlayerIds = lineUp.getSquadPlayers().stream()
                .map(SquadPlayer::getId)
                .toList();

        // 2. 삭제할 플레이어 (기존에는 있었지만 현재 없는)
        final List<String> playersToDelete = existingPlayerIds.stream()
                .filter(id -> !currentPlayerIds.contains(id))
                .toList();

        if (!playersToDelete.isEmpty()) {
            squadPlayersDao.ctx()
                    .deleteFrom(SQUAD_PLAYERS)
                    .where(SQUAD_PLAYERS.ID.in(playersToDelete))
                    .execute();
        }

        // 3. 추가/수정할 플레이어 (UPSERT)
        if (!lineUp.getSquadPlayers().isEmpty()) {
            upsertPlayers(lineUp.getSquadPlayers());
        }
    }

    private void upsertPlayers(final List<SquadPlayer> squadPlayers) {
        if (squadPlayers.isEmpty()) {
            return;
        }

        var query = squadPlayersDao.ctx().insertInto(SQUAD_PLAYERS,
                SQUAD_PLAYERS.ID,
                SQUAD_PLAYERS.USER_ID,
                SQUAD_PLAYERS.SQUAD_ID,
                SQUAD_PLAYERS.POSITIONS,
                SQUAD_PLAYERS.BIRTH_DATE,
                SQUAD_PLAYERS.HEIGHT,
                SQUAD_PLAYERS.WEIGHT,
                SQUAD_PLAYERS.FOOT,
                SQUAD_PLAYERS.BACK_NUMBER,
                SQUAD_PLAYERS.CREATED_AT,
                SQUAD_PLAYERS.UPDATED_AT,
                SQUAD_PLAYERS.DELETED_AT
        );

        for (final SquadPlayer squadPlayer : squadPlayers) {
            query.values(
                    squadPlayer.getId(),
                    squadPlayer.getUserId(),
                    squadPlayer.getSquadId(),
                    Objects.nonNull(squadPlayer.getPositions())
                            ? squadPlayer.getPositions().stream().map(Enum::name).collect(Collectors.joining(","))
                            : null,
                    Objects.nonNull(squadPlayer.getBirthDate()) ? squadPlayer.getBirthDate().birthDate() : null,
                    squadPlayer.getHeight(),
                    squadPlayer.getWeight(),
                    Objects.nonNull(squadPlayer.getFoot()) ? squadPlayer.getFoot().name() : null,
                    squadPlayer.getBackNumber().value(),
                    squadPlayer.getCreatedAt(),
                    squadPlayer.getUpdatedAt(),
                    squadPlayer.getDeletedAt()
            );
        }

        query.onDuplicateKeyUpdate()
                .set(SQUAD_PLAYERS.POSITIONS, excluded(SQUAD_PLAYERS.POSITIONS))
                .set(SQUAD_PLAYERS.BIRTH_DATE, excluded(SQUAD_PLAYERS.BIRTH_DATE))
                .set(SQUAD_PLAYERS.HEIGHT, excluded(SQUAD_PLAYERS.HEIGHT))
                .set(SQUAD_PLAYERS.WEIGHT, excluded(SQUAD_PLAYERS.WEIGHT))
                .set(SQUAD_PLAYERS.FOOT, excluded(SQUAD_PLAYERS.FOOT))
                .set(SQUAD_PLAYERS.BACK_NUMBER, excluded(SQUAD_PLAYERS.BACK_NUMBER))
                .set(SQUAD_PLAYERS.UPDATED_AT, excluded(SQUAD_PLAYERS.UPDATED_AT))
                .set(SQUAD_PLAYERS.DELETED_AT, excluded(SQUAD_PLAYERS.DELETED_AT))
                .execute();
    }

    private List<SquadPlayer> findPlayersBySquadId(final String squadId) {
        return squadPlayersDao.ctx()
                .selectFrom(SQUAD_PLAYERS)
                .where(SQUAD_PLAYERS.SQUAD_ID.eq(squadId))
                .fetchInto(SquadPlayersEntity.class)
                .stream()
                .map(JOOQSquadRepository::playerDomain)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static Squad domain(final SquadsEntity entity, final List<SquadPlayer> squadPlayers) {
        return new Squad(
                entity.getId(),
                entity.getClubId(),
                squadPlayers,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private static SquadPlayer playerDomain(final SquadPlayersEntity entity) {
        return new SquadPlayer(
                entity.getId(),
                entity.getSquadId(),
                entity.getUserId(),
                Objects.nonNull(entity.getBirthDate()) ? new BirthDate(entity.getBirthDate()): null,
                entity.getHeight(),
                entity.getWeight(),
                Objects.nonNull(entity.getFoot()) ? Foot.valueOf(entity.getFoot()) : null,
                Objects.nonNull(entity.getPositions())
                        ? Arrays.stream(entity.getPositions().split(",")).map(Position::valueOf).toList()
                        : null,
                new BackNumber(entity.getBackNumber()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}
