package com.official.lockr.domain.club.sport.football.squad.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.sport.football.squad.api.dto.UpdateSquadPlayerRequest;
import com.official.lockr.domain.club.sport.football.squad.application.command.UpdateSquadPlayerCommand;
import com.official.lockr.domain.club.sport.football.squad.application.usecase.UpdateSquadPlayerUseCase;
import com.official.lockr.domain.club.sport.football.squad.domain.Squad;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/clubs/{clubId}/squads/{squadId}")
@RestController
public class SquadApi {

    private final UpdateSquadPlayerUseCase updateSquadPlayerUseCase;

    public SquadApi(final UpdateSquadPlayerUseCase updateSquadPlayerUseCase) {
        this.updateSquadPlayerUseCase = updateSquadPlayerUseCase;
    }

    @PostMapping("/players")
    public ResponseEntity<Squad> registerOrUpdatePlayer(
            @PathVariable String clubId,
            @PathVariable String squadId,
            @RequestBody UpdateSquadPlayerRequest request,
            @RequestAttribute("signInSession") final SignInSession signInSession
    ) {
        final UpdateSquadPlayerCommand command = request.toCommand(signInSession.userId(), clubId);
        final Squad squad = updateSquadPlayerUseCase.updatePlayer(command);
        return ResponseEntity.ok(squad);
    }
}
