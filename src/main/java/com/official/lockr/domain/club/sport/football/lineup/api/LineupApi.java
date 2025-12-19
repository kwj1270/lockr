package com.official.lockr.domain.club.sport.football.lineup.api;

import com.official.lockr.domain.club.sport.football.lineup.api.dto.AssignSlotRequest;
import com.official.lockr.domain.club.sport.football.lineup.application.usecase.AssignSlotUseCase;
import com.official.lockr.domain.club.sport.football.lineup.domain.Lineup;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(value = "/api/v1/clubs/{clubId}/lineups")
@RestController
public class LineupApi {

    private final AssignSlotUseCase assignSlotUseCase;

    public LineupApi(final AssignSlotUseCase assignSlotUseCase) {
        this.assignSlotUseCase = assignSlotUseCase;
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
}
