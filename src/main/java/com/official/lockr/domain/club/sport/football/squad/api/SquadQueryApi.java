package com.official.lockr.domain.club.sport.football.squad.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.sport.football.squad.domain.Squad;
import com.official.lockr.domain.club.sport.football.squad.domain.SquadPlayer;
import com.official.lockr.global.vo.BackNumber;
import com.official.lockr.global.vo.BirthDate;
import com.official.lockr.global.vo.Foot;
import com.official.lockr.global.vo.Position;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpSession;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.SquadPlayersDao;
import org.jooq.generated.tables.daos.SquadsDao;
import org.jooq.generated.tables.pojos.SquadPlayersEntity;
import org.jooq.generated.tables.pojos.SquadsEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;
import static org.jooq.generated.tables.SquadPlayersJOOQEntity.SQUAD_PLAYERS;
import static org.jooq.generated.tables.SquadsJOOQEntity.SQUADS;

@RequestMapping("/api/v1/clubs/{clubId}/squads")
@RestController
public class SquadQueryApi {

    private final SquadsDao squadsDao;
    private final SquadPlayersDao squadPlayersDao;

    public SquadQueryApi(final Configuration configuration) {
        this.squadsDao = new SquadsDao(configuration);
        this.squadPlayersDao = new SquadPlayersDao(configuration);
    }

    @GetMapping
    public ResponseEntity<Squad> find(
            @PathVariable String clubId
    ) {
        final Squad squad = findByClubId(clubId);
        return ResponseEntity.ok(squad);
    }

    @GetMapping("/me")
    public ResponseEntity<SquadPlayer> findMySquadPlayer(
            @PathVariable String clubId,
            final HttpSession httpSession
    ) {
        final SignInSession signIn = session(httpSession);
        final SquadPlayer mySquadPlayer = findMySquadPlayerByUserIdAndClubId(signIn.userId(), clubId);

        if (mySquadPlayer == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mySquadPlayer);
    }



    @Nullable
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

    private List<SquadPlayer> findPlayersBySquadId(final String squadId) {
        return squadPlayersDao.ctx()
                .selectFrom(SQUAD_PLAYERS)
                .where(SQUAD_PLAYERS.SQUAD_ID.eq(squadId))
                .fetchInto(SquadPlayersEntity.class)
                .stream()
                .map(SquadQueryApi::playerDomain)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static SquadPlayer playerDomain(final SquadPlayersEntity entity) {
        return new SquadPlayer(
                entity.getId(),
                entity.getSquadId(),
                entity.getUserId(),
                entity.getName(),
                entity.getProfileImage(),
                Objects.nonNull(entity.getBirthDate()) ? new BirthDate(entity.getBirthDate()) : null,
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

    @Nullable
    private SquadPlayer findMySquadPlayerByUserIdAndClubId(final String userId, final String clubId) {
        // Find the squad for this club
        final SquadsEntity squadEntity = squadsDao.ctx()
                .selectFrom(SQUADS)
                .where(SQUADS.CLUB_ID.eq(clubId))
                .and(SQUADS.DELETED_AT.isNull())
                .fetchOptional()
                .map(record -> new SquadsEntity(
                        record.getId(),
                        record.getClubId(),
                        record.getCreatedAt(),
                        record.getUpdatedAt(),
                        record.getDeletedAt()
                ))
                .orElse(null);

        if (squadEntity == null) {
            return null;
        }
        // Find the SquadPlayer for this member
        final SquadPlayersEntity playerEntity = squadPlayersDao.ctx()
                .selectFrom(SQUAD_PLAYERS)
                .where(SQUAD_PLAYERS.SQUAD_ID.eq(squadEntity.getId()))
                .and(SQUAD_PLAYERS.USER_ID.eq(userId))
                .and(SQUAD_PLAYERS.DELETED_AT.isNull())
                .fetchOneInto(SquadPlayersEntity.class);

        if (playerEntity == null) {
            return null;
        }

        return playerDomain(playerEntity);
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(signIn)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return signIn;
    }
}
