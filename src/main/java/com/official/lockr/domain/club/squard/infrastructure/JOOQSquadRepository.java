package com.official.lockr.domain.club.squard.infrastructure;

import com.official.lockr.domain.club.common.Foot;
import com.official.lockr.domain.club.common.Position;
import com.official.lockr.domain.club.squard.domain.squad.Player;
import com.official.lockr.domain.club.squard.domain.squad.Squad;
import com.official.lockr.domain.club.squard.domain.squad.SquadRepository;
import com.official.lockr.domain.club.squard.domain.squad.vo.BackNumber;
import com.official.lockr.domain.club.squard.domain.squad.vo.PlayerRole;
import com.official.lockr.global.ddd.DomainEventPublisher;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.PlayersDao;
import org.jooq.generated.tables.daos.SquadsDao;
import org.jooq.generated.tables.pojos.PlayersEntity;
import org.jooq.generated.tables.pojos.SquadsEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.jooq.generated.tables.PlayersJOOQEntity.PLAYERS;
import static org.jooq.generated.tables.SquadsJOOQEntity.SQUADS;
import static org.jooq.impl.DSL.excluded;

@Repository
public class JOOQSquadRepository implements SquadRepository {

    private final SquadsDao squadsDao;
    private final PlayersDao playersDao;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQSquadRepository(final Configuration configuration,
                               final DomainEventPublisher domainEventPublisher) {
        this.squadsDao = new SquadsDao(configuration);
        this.playersDao = new PlayersDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Nullable
    @Override
    public Squad findByTeamId(final String teamId) {
        final SquadsEntity squadEntity = squadsDao.ctx()
                .selectFrom(SQUADS)
                .where(SQUADS.TEAM_ID.eq(teamId))
                .fetchOptional()
                .map(record -> new SquadsEntity(
                        record.getId(),
                        record.getTeamId(),
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

    private void upsertSquad(final Squad squad) {
        squadsDao.ctx()
                .insertInto(SQUADS)
                .set(SQUADS.ID, squad.getId())
                .set(SQUADS.TEAM_ID, squad.getTeamId())
                .set(SQUADS.CREATED_AT, squad.getCreatedAt())
                .set(SQUADS.UPDATED_AT, squad.getUpdatedAt())
                .set(SQUADS.DELETED_AT, squad.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(SQUADS.UPDATED_AT, squad.getUpdatedAt())
                .set(SQUADS.DELETED_AT, squad.getDeletedAt())
                .execute();
    }

    private void syncPlayers(final Squad squad) {
        // 1. 기존 플레이어 조회
        final List<String> existingPlayerIds = playersDao.ctx()
                .select(PLAYERS.ID)
                .from(PLAYERS)
                .where(PLAYERS.SQUAD_ID.eq(squad.getId()))
                .fetchInto(String.class);

        final List<String> currentPlayerIds = squad.getPlayers().stream()
                .map(Player::getId)
                .toList();

        // 2. 삭제할 플레이어 (기존에는 있었지만 현재 없는)
        final List<String> playersToDelete = existingPlayerIds.stream()
                .filter(id -> !currentPlayerIds.contains(id))
                .toList();

        if (!playersToDelete.isEmpty()) {
            playersDao.ctx()
                    .deleteFrom(PLAYERS)
                    .where(PLAYERS.ID.in(playersToDelete))
                    .execute();
        }

        // 3. 추가/수정할 플레이어 (UPSERT)
        if (!squad.getPlayers().isEmpty()) {
            upsertPlayers(squad.getPlayers());
        }
    }

    private void upsertPlayers(final List<Player> players) {
        if (players.isEmpty()) {
            return;
        }

        var query = playersDao.ctx().insertInto(PLAYERS,
                PLAYERS.ID,
                PLAYERS.MEMBER_ID,
                PLAYERS.SQUAD_ID,
                PLAYERS.PROFILE_IMAGE,
                PLAYERS.NAME,
                PLAYERS.NATIONALITY,
                PLAYERS.POSITIONS,
                PLAYERS.BIRTH,
                PLAYERS.HEIGHT,
                PLAYERS.WEIGHT,
                PLAYERS.FOOT,
                PLAYERS.BACK_NUMBER,
                PLAYERS.PLAYER_ROLE,
                PLAYERS.CREATED_AT,
                PLAYERS.UPDATED_AT,
                PLAYERS.DELETED_AT
        );

        for (final Player player : players) {
            query.values(
                    player.getId(),
                    player.getMemberId(),
                    player.getSquadId(),
                    player.getProfileImage(),
                    player.getName(),
                    player.getNationality(),
                    Objects.nonNull(player.getPositions())
                            ? player.getPositions().stream().map(Enum::name).collect(Collectors.joining(","))
                            : null,
                    player.getBirth(),
                    player.getHeight(),
                    player.getWeight(),
                    Objects.nonNull(player.getFoot()) ? player.getFoot().name() : null,
                    player.getBackNumber().getValue(),
                    player.getPlayerRole().name(),
                    player.getCreatedAt(),
                    player.getUpdatedAt(),
                    player.getDeletedAt()
            );
        }

        query.onDuplicateKeyUpdate()
                .set(PLAYERS.PROFILE_IMAGE, excluded(PLAYERS.PROFILE_IMAGE))
                .set(PLAYERS.NAME, excluded(PLAYERS.NAME))
                .set(PLAYERS.NATIONALITY, excluded(PLAYERS.NATIONALITY))
                .set(PLAYERS.POSITIONS, excluded(PLAYERS.POSITIONS))
                .set(PLAYERS.BIRTH, excluded(PLAYERS.BIRTH))
                .set(PLAYERS.HEIGHT, excluded(PLAYERS.HEIGHT))
                .set(PLAYERS.WEIGHT, excluded(PLAYERS.WEIGHT))
                .set(PLAYERS.FOOT, excluded(PLAYERS.FOOT))
                .set(PLAYERS.BACK_NUMBER, excluded(PLAYERS.BACK_NUMBER))
                .set(PLAYERS.PLAYER_ROLE, excluded(PLAYERS.PLAYER_ROLE))
                .set(PLAYERS.UPDATED_AT, excluded(PLAYERS.UPDATED_AT))
                .set(PLAYERS.DELETED_AT, excluded(PLAYERS.DELETED_AT))
                .execute();
    }

    private List<Player> findPlayersBySquadId(final String squadId) {
        return playersDao.ctx()
                .selectFrom(PLAYERS)
                .where(PLAYERS.SQUAD_ID.eq(squadId))
                .fetchInto(PlayersEntity.class)
                .stream()
                .map(JOOQSquadRepository::playerDomain)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static Squad domain(final SquadsEntity entity, final List<Player> players) {
        return new Squad(
                entity.getId(),
                entity.getTeamId(),
                players,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private static Player playerDomain(final PlayersEntity entity) {
        return new Player(
                entity.getId(),
                entity.getSquadId(),
                entity.getMemberId(),
                entity.getProfileImage(),
                entity.getName(),
                entity.getNationality(),
                Objects.nonNull(entity.getPositions())
                        ? Arrays.stream(entity.getPositions().split(",")).map(Position::valueOf).toList()
                        : null,
                entity.getBirth(),
                entity.getHeight(),
                entity.getWeight(),
                Objects.nonNull(entity.getFoot()) ? Foot.valueOf(entity.getFoot()) : null,
                new BackNumber(entity.getBackNumber()),
                PlayerRole.valueOf(entity.getPlayerRole()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}
