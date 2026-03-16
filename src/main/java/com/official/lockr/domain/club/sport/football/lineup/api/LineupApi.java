package com.official.lockr.domain.club.sport.football.lineup.api;

import com.official.lockr.domain.club.sport.football.lineup.api.dto.AssignSlotRequest;
import com.official.lockr.domain.club.sport.football.lineup.api.dto.ChangeFormationRequest;
import com.official.lockr.domain.club.sport.football.lineup.api.dto.RemoveSlotRequest;
import com.official.lockr.domain.club.sport.football.lineup.application.usecase.AssignSlotUseCase;
import com.official.lockr.domain.club.sport.football.lineup.application.usecase.ChangeFormationUseCase;
import com.official.lockr.domain.club.sport.football.lineup.application.usecase.RemoveSlotUseCase;
import com.official.lockr.domain.club.sport.football.lineup.domain.Lineup;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(value = "/api/v1/clubs/{clubId}/lineups")
@RestController
public class LineupApi {

    private final AssignSlotUseCase assignSlotUseCase;
    private final ChangeFormationUseCase changeFormation;
    private final RemoveSlotUseCase removeSlotUseCase;

    public LineupApi(final AssignSlotUseCase assignSlotUseCase,
                     final ChangeFormationUseCase changeFormation,
                     final RemoveSlotUseCase removeSlotUseCase) {
        this.assignSlotUseCase = assignSlotUseCase;
        this.changeFormation = changeFormation;
        this.removeSlotUseCase = removeSlotUseCase;
    }

    @PostMapping("/{lineupId}/slots")
    public ResponseEntity<Lineup> assignSlot(
            @PathVariable String clubId,
            @PathVariable String lineupId,
            @RequestBody AssignSlotRequest request
    ) {
        final Lineup lineup = assignSlotUseCase.assign(clubId, lineupId, request.slotType(), request.slotIndex(), request.squadPlayerId());
        return ResponseEntity.ok(lineup);
    }

    @PostMapping("/{lineupId}/formations")
    public ResponseEntity<Lineup> changeFormation(
            @PathVariable String clubId,
            @PathVariable String lineupId,
            @RequestBody ChangeFormationRequest request
    ) {
        final Lineup lineup = changeFormation.changeFormation(clubId, lineupId, request.formation());
        return ResponseEntity.ok(lineup);
    }

    @PostMapping("/{lineupId}/slots/remove")
    public ResponseEntity<Lineup> removeSlot(
            @PathVariable String clubId,
            @PathVariable String lineupId,
            @RequestBody RemoveSlotRequest request
    ) {
        final Lineup lineup = removeSlotUseCase.removeSlot(clubId, lineupId, request.slotType(), request.slotIndex());
        return ResponseEntity.ok(lineup);
    }
}
