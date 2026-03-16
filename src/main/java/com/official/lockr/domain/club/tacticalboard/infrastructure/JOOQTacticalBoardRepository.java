package com.official.lockr.domain.club.tacticalboard.infrastructure;

import com.official.lockr.domain.club.tacticalboard.domain.entry.player.*;
import com.official.lockr.global.vo.Location;
import com.official.lockr.global.vo.Position;
import com.official.lockr.global.vo.Formation;
import com.official.lockr.domain.club.tacticalboard.domain.entry.Entry;
import com.official.lockr.domain.club.tacticalboard.domain.TacticalBoard;
import com.official.lockr.domain.club.tacticalboard.domain.TacticalBoardRepository;
import com.official.lockr.global.ddd.DomainEventPublisher;
import com.official.lockr.global.util.UlidUtils;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.*;
import org.jooq.generated.tables.pojos.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.jooq.generated.Tables.FORMATIONS;
import static org.jooq.generated.tables.BenchPlayersJOOQEntity.BENCH_PLAYERS;
import static org.jooq.generated.tables.FieldPlayersJOOQEntity.FIELD_PLAYERS;
import static org.jooq.generated.tables.NoneSelectedPlayersJOOQEntity.NONE_SELECTED_PLAYERS;
import static org.jooq.generated.tables.TacticalBoardsJOOQEntity.TACTICAL_BOARDS;
import static org.jooq.impl.DSL.excluded;

@Repository
public class JOOQTacticalBoardRepository implements TacticalBoardRepository {

    private final TacticalBoardsDao tacticalBoardsDao;
    private final FieldPlayersDao fieldPlayersDao;
    private final BenchPlayersDao benchPlayersDao;
    private final NoneSelectedPlayersDao noneSelectedPlayersDao;
    private final FormationsDao formationsDao;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQTacticalBoardRepository(final Configuration configuration,
                                       final DomainEventPublisher domainEventPublisher) {
        this.tacticalBoardsDao = new TacticalBoardsDao(configuration);
        this.fieldPlayersDao = new FieldPlayersDao(configuration);
        this.benchPlayersDao = new BenchPlayersDao(configuration);
        this.noneSelectedPlayersDao = new NoneSelectedPlayersDao(configuration);
        this.formationsDao = new FormationsDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Nullable
    @Override
    public TacticalBoard findById(final String id) {
        final TacticalBoardsEntity entity = tacticalBoardsDao.ctx()
                .selectFrom(TACTICAL_BOARDS)
                .where(TACTICAL_BOARDS.ID.eq(id))
                .fetchInto(TacticalBoardsEntity.class)
                .stream()
                .findFirst()
                .orElse(null);

        if (entity == null) {
            return null;
        }

        return toDomain(entity, id);
    }

    @Override
    public List<TacticalBoard> findAllByClubId(final String clubId) {
        final List<TacticalBoardsEntity> entities = tacticalBoardsDao.ctx()
                .selectFrom(TACTICAL_BOARDS)
                .where(TACTICAL_BOARDS.CLUB_ID.eq(clubId))
                .fetchInto(TacticalBoardsEntity.class);

        return entities.stream()
                .map(entity -> toDomain(entity, entity.getId()))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public TacticalBoard save(final TacticalBoard tacticalBoard) {
        upsertEntry(tacticalBoard);

        if (tacticalBoard.getLineup() != null) {
            syncPlayers(tacticalBoard);
        }

        tacticalBoard.publish(domainEventPublisher);
        return tacticalBoard;
    }

    @Transactional
    @Override
    public void delete(final String id) {
        // FK CASCADE로 자동 삭제되므로 tactical_boards만 삭제
        tacticalBoardsDao.ctx()
                .deleteFrom(TACTICAL_BOARDS)
                .where(TACTICAL_BOARDS.ID.eq(id))
                .execute();
    }

    private void upsertEntry(final TacticalBoard tacticalBoard) {
        tacticalBoardsDao.ctx()
                .insertInto(TACTICAL_BOARDS)
                .set(TACTICAL_BOARDS.ID, tacticalBoard.getId())
                .set(TACTICAL_BOARDS.NAME, tacticalBoard.getName())
                .set(TACTICAL_BOARDS.CLUB_ID, tacticalBoard.getClubId())
                .set(TACTICAL_BOARDS.COACH_USER_ID, tacticalBoard.getCoachUserId())
                .set(TACTICAL_BOARDS.CREATED_AT, tacticalBoard.getCreatedAt())
                .set(TACTICAL_BOARDS.UPDATED_AT, tacticalBoard.getUpdatedAt())
                .set(TACTICAL_BOARDS.DELETED_AT, tacticalBoard.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(TACTICAL_BOARDS.NAME, excluded(TACTICAL_BOARDS.NAME))
                .set(TACTICAL_BOARDS.COACH_USER_ID, excluded(TACTICAL_BOARDS.COACH_USER_ID))
                .set(TACTICAL_BOARDS.UPDATED_AT, excluded(TACTICAL_BOARDS.UPDATED_AT))
                .set(TACTICAL_BOARDS.DELETED_AT, excluded(TACTICAL_BOARDS.DELETED_AT))
                .execute();
    }

    private void syncPlayers(final TacticalBoard tacticalBoard) {
        final String tacticalBoardId = tacticalBoard.getId();
        final Entry entry = tacticalBoard.getLineup();

        // 1. 기존 플레이어들 및 포메이션 삭제
        deleteAllPlayers(tacticalBoardId);
        deleteFormation(tacticalBoardId);

        // 2. 새로운 플레이어들 삽입
        insertFieldPlayers(tacticalBoardId, entry.getFieldPlayers().getFiledPlayers());
        insertBenchPlayers(tacticalBoardId, entry.getBenchPlayers().getBenchPlayers());
        insertNoneSelectedPlayers(tacticalBoardId, entry.getNoneSelectedPlayers().getNoneSelectedPlayers());

        // 3. 포메이션 삽입
        insertFormation(tacticalBoardId, entry.getFormation());
    }

    private void deleteAllPlayers(final String tacticalBoardId) {
        fieldPlayersDao.ctx()
                .deleteFrom(FIELD_PLAYERS)
                .where(FIELD_PLAYERS.TACTICAL_BOARD_ID.eq(tacticalBoardId))
                .execute();

        benchPlayersDao.ctx()
                .deleteFrom(BENCH_PLAYERS)
                .where(BENCH_PLAYERS.TACTICAL_BOARD_ID.eq(tacticalBoardId))
                .execute();

        noneSelectedPlayersDao.ctx()
                .deleteFrom(NONE_SELECTED_PLAYERS)
                .where(NONE_SELECTED_PLAYERS.TACTICAL_BOARD_ID.eq(tacticalBoardId))
                .execute();
    }

    private void insertFieldPlayers(final String tacticalBoardId, final List<FieldPlayer> fieldPlayers) {
        if (fieldPlayers.isEmpty()) {
            return;
        }

        var query = fieldPlayersDao.ctx().insertInto(FIELD_PLAYERS,
                FIELD_PLAYERS.ID,
                FIELD_PLAYERS.TACTICAL_BOARD_ID,
                FIELD_PLAYERS.SQUAD_PLAYER_ID,
                FIELD_PLAYERS.POSITION,
                FIELD_PLAYERS.LOCATION_X,
                FIELD_PLAYERS.LOCATION_Y,
                FIELD_PLAYERS.IS_CAPTAIN
        );

        for (final FieldPlayer player : fieldPlayers) {
            query.values(
                    UlidUtils.generateUlid(),
                    tacticalBoardId,
                    player.getSquadPlayerId(),
                    player.getPosition().name(),
                    player.getLocation().x(),
                    player.getLocation().y(),
                    player.isCaptain()
            );
        }

        query.execute();
    }

    private void insertBenchPlayers(final String tacticalBoardId, final List<BenchPlayer> benchPlayers) {
        if (benchPlayers.isEmpty()) {
            return;
        }

        var query = benchPlayersDao.ctx().insertInto(BENCH_PLAYERS,
                BENCH_PLAYERS.ID,
                BENCH_PLAYERS.TACTICAL_BOARD_ID,
                BENCH_PLAYERS.SQUAD_PLAYER_ID,
                BENCH_PLAYERS.POSITION
        );

        for (final BenchPlayer player : benchPlayers) {
            query.values(
                    UlidUtils.generateUlid(),
                    tacticalBoardId,
                    player.getSquadPlayerId(),
                    player.getPosition().name()
            );
        }

        query.execute();
    }

    private void insertNoneSelectedPlayers(final String tacticalBoardId, final List<NoneSelectedPlayer> noneSelectedPlayers) {
        if (noneSelectedPlayers.isEmpty()) {
            return;
        }

        var query = noneSelectedPlayersDao.ctx().insertInto(NONE_SELECTED_PLAYERS,
                NONE_SELECTED_PLAYERS.ID,
                NONE_SELECTED_PLAYERS.TACTICAL_BOARD_ID,
                NONE_SELECTED_PLAYERS.SQUAD_PLAYER_ID,
                NONE_SELECTED_PLAYERS.POSITION
        );

        for (final NoneSelectedPlayer player : noneSelectedPlayers) {
            query.values(
                    UlidUtils.generateUlid(),
                    tacticalBoardId,
                    player.getSquadPlayerId(),
                    player.getPosition().name()
            );
        }

        query.execute();
    }

    private TacticalBoard toDomain(final TacticalBoardsEntity entity, final String tacticalBoardId) {
        final List<FieldPlayer> fieldPlayers = findFieldPlayersByTacticalBoardId(tacticalBoardId);
        final List<BenchPlayer> benchPlayers = findBenchPlayersByTacticalBoardId(tacticalBoardId);
        final List<NoneSelectedPlayer> noneSelectedPlayers = findNoneSelectedPlayersByTacticalBoardId(tacticalBoardId);
        final Formation formation = findFormation(tacticalBoardId);
        final Entry entry = new Entry(
                new FieldPlayers(fieldPlayers),
                new BenchPlayers(benchPlayers),
                new NoneSelectedPlayers(noneSelectedPlayers),
                formation
        );

        return new TacticalBoard(
                entity.getId(),
                entity.getClubId(),
                entity.getCoachUserId(),
                entity.getName(),
                entry,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private Formation findFormation(final String tacticalBoardId) {
        final FormationsEntity formationsEntity = formationsDao.ctx()
                .selectFrom(FORMATIONS)
                .where(FORMATIONS.TACTICAL_BOARD_ID.eq(tacticalBoardId))
                .fetchOneInto(FormationsEntity.class);
        if(isNull(formationsEntity)) {
            return Formation.FORMATION_4_3_3;
        }
        return Formation.of(formationsEntity.getFormation());
    }

    private List<FieldPlayer> findFieldPlayersByTacticalBoardId(final String tacticalBoardId) {
        return fieldPlayersDao.ctx()
                .selectFrom(FIELD_PLAYERS)
                .where(FIELD_PLAYERS.TACTICAL_BOARD_ID.eq(tacticalBoardId))
                .fetchInto(FieldPlayersEntity.class)
                .stream()
                .map(entity -> new FieldPlayer(
                        entity.getSquadPlayerId(),
                        Position.valueOf(entity.getPosition()),
                        new Location(entity.getLocationX(), entity.getLocationY()),
                        entity.getIsCaptain()
                ))
                .collect(Collectors.toList());
    }

    private List<BenchPlayer> findBenchPlayersByTacticalBoardId(final String tacticalBoardId) {
        return benchPlayersDao.ctx()
                .selectFrom(BENCH_PLAYERS)
                .where(BENCH_PLAYERS.TACTICAL_BOARD_ID.eq(tacticalBoardId))
                .fetchInto(BenchPlayersEntity.class)
                .stream()
                .map(entity -> new BenchPlayer(
                        entity.getSquadPlayerId(),
                        Position.valueOf(entity.getPosition())
                ))
                .collect(Collectors.toList());
    }

    private List<NoneSelectedPlayer> findNoneSelectedPlayersByTacticalBoardId(final String tacticalBoardId) {
        return noneSelectedPlayersDao.ctx()
                .selectFrom(NONE_SELECTED_PLAYERS)
                .where(NONE_SELECTED_PLAYERS.TACTICAL_BOARD_ID.eq(tacticalBoardId))
                .fetchInto(NoneSelectedPlayersEntity.class)
                .stream()
                .map(entity -> new NoneSelectedPlayer(
                        entity.getSquadPlayerId(),
                        Position.valueOf(entity.getPosition())
                ))
                .collect(Collectors.toList());
    }

    private void deleteFormation(final String tacticalBoardId) {
        formationsDao.ctx()
                .deleteFrom(FORMATIONS)
                .where(FORMATIONS.TACTICAL_BOARD_ID.eq(tacticalBoardId))
                .execute();
    }

    private void insertFormation(final String tacticalBoardId, final Formation formation) {
        if (formation == null) {
            return;
        }

        formationsDao.ctx()
                .insertInto(FORMATIONS)
                .set(FORMATIONS.ID, UlidUtils.generateUlid())
                .set(FORMATIONS.TACTICAL_BOARD_ID, tacticalBoardId)
                .set(FORMATIONS.FORMATION, formation.getName())
                .execute();
    }
}
