package com.official.lockr.domain.club.sport.football.lineup.api;

import com.official.lockr.domain.club.sport.football.lineup.api.dto.*;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.*;
import org.jooq.generated.tables.pojos.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.jooq.generated.tables.LineupSlotsJOOQEntity.LINEUP_SLOTS;
import static org.jooq.generated.tables.LineupsJOOQEntity.LINEUPS;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;
import static org.jooq.generated.tables.SquadPlayersJOOQEntity.SQUAD_PLAYERS;
import static org.jooq.generated.tables.SquadsJOOQEntity.SQUADS;

@RequestMapping(value = "/api/v1/clubs/{clubId}/lineups")
@RestController
public class LineupQueryApi {

    private final LineupsDao lineupsDao;
    private final LineupSlotsDao lineupPlayersDao;
    private final SquadsDao squadsDao;
    private final SquadPlayersDao squadPlayersDao;
    private final MembersDao membersDao;

    public LineupQueryApi(final Configuration configuration) {
        this.lineupsDao = new LineupsDao(configuration);
        this.lineupPlayersDao = new LineupSlotsDao(configuration);
        this.squadsDao = new SquadsDao(configuration);
        this.squadPlayersDao = new SquadPlayersDao(configuration);
        this.membersDao = new MembersDao(configuration);
    }

    @GetMapping
    public ResponseEntity<LineupsResponse> lineups(
            @PathVariable String clubId
    ) {
        final List<LineupsEntity> lineups = lineupsDao.ctx()
                .selectFrom(LINEUPS)
                .where(LINEUPS.CLUB_ID.eq(clubId))
                .fetchInto(LineupsEntity.class);

        final SquadsEntity squad = squadsDao.ctx()
                .selectFrom(SQUADS)
                .where(SQUADS.CLUB_ID.eq(clubId))
                .fetchOneInto(SquadsEntity.class);

        if (squad == null) {
            return ResponseEntity.ok(new LineupsResponse(List.of(), List.of()));
        }

        final List<SquadPlayersEntity> squadPlayers = squadPlayersDao.ctx()
                .selectFrom(SQUAD_PLAYERS)
                .where(SQUAD_PLAYERS.SQUAD_ID.eq(squad.getId()))
                .fetchInto(SquadPlayersEntity.class);

        final List<MembersEntity> members = membersDao.ctx()
                .selectFrom(MEMBERS)
                .where(MEMBERS.CLUB_ID.eq(clubId))
                .fetchInto(MembersEntity.class);

        final Map<String, SquadPlayersEntity> squadPlayerMap = squadPlayers.stream()
                .collect(Collectors.toMap(SquadPlayersEntity::getId, sp -> sp));

        final Map<String, MembersEntity> memberMap = members.stream()
                .collect(Collectors.toMap(MembersEntity::getUserId, m -> m));

        final List<SquadPlayerResponse> squadPlayerResponses = createSquadPlayers(squadPlayers, memberMap);
        final List<LineupResponse> lineupResponses = lineups.stream()
                .map(lineup -> createLineupResponse(lineup, squadPlayerMap, squadPlayerResponses))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new LineupsResponse(lineupResponses, squadPlayerResponses));
    }

    private LineupResponse createLineupResponse(
            final LineupsEntity lineup,
            final Map<String, SquadPlayersEntity> squadPlayerMap,
            final List<SquadPlayerResponse> memberPool
    ) {
        final List<LineupSlotsEntity> lineupPlayers = lineupPlayersDao.ctx()
                .selectFrom(LINEUP_SLOTS)
                .where(LINEUP_SLOTS.LINEUP_ID.eq(lineup.getId()))
                .fetchInto(LineupSlotsEntity.class);

        final Map<Integer, LineupSlotsEntity> starterMap = lineupPlayers.stream()
                .filter(lp -> "starter".equals(lp.getSlotType()))
                .collect(Collectors.toMap(LineupSlotsEntity::getSlotIndex, lp -> lp));

        final Map<Integer, LineupSlotsEntity> substituteMap = lineupPlayers.stream()
                .filter(lp -> "substitute".equals(lp.getSlotType()))
                .collect(Collectors.toMap(LineupSlotsEntity::getSlotIndex, lp -> lp));

        final List<SlotResponse> starters = IntStream.rangeClosed(0, 10)
                .mapToObj(index -> createSlotResponse(index, starterMap.get(index), squadPlayerMap))
                .collect(Collectors.toList());

        final List<SlotResponse> substitutes = IntStream.rangeClosed(0, 6)
                .mapToObj(index -> createSlotResponse(index, substituteMap.get(index), squadPlayerMap))
                .collect(Collectors.toList());

        return new LineupResponse(
                lineup.getId(),
                lineup.getName(),
                lineup.getFormation(),
                starters,
                substitutes
        );
    }

    private SlotResponse createSlotResponse(
            final int slotIndex,
            final LineupSlotsEntity lineupPlayer,
            final Map<String, SquadPlayersEntity> squadPlayerMap
    ) {
        if (lineupPlayer == null) {
            return new SlotResponse(slotIndex, null);
        }

        final SquadPlayersEntity squadPlayer = squadPlayerMap.get(lineupPlayer.getSquadPlayerId());
        if (squadPlayer == null) {
            return new SlotResponse(slotIndex, null);
        }

        final PlayerInSlotResponse player = new PlayerInSlotResponse(
                squadPlayer.getId(),
                squadPlayer.getName(),
                squadPlayer.getBackNumber(),
                squadPlayer.getPositions() != null && !squadPlayer.getPositions().isEmpty()
                        ? squadPlayer.getPositions().split(",")[0]
                        : null
        );

        return new SlotResponse(slotIndex, player);
    }

    private List<SquadPlayerResponse> createSquadPlayers(
            final List<SquadPlayersEntity> squadPlayers,
            final Map<String, MembersEntity> memberMap
    ) {
        return squadPlayers.stream()
                .map(squadPlayer -> {
                    final MembersEntity member = memberMap.get(squadPlayer.getUserId());
                    return new SquadPlayerResponse(
                            squadPlayer.getId(),
                            squadPlayer.getName(),
                            squadPlayer.getBackNumber(),
                            squadPlayer.getPositions() != null && !squadPlayer.getPositions().isEmpty()
                                    ? squadPlayer.getPositions().split(",")[0]
                                    : null,
                            member != null ? member.getMemberRole() : null
                    );
                })
                .collect(Collectors.toList());
    }
}

