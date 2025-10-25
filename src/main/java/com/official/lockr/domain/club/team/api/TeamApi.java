package com.official.lockr.domain.club.team.api;

import com.official.lockr.domain.auth.domain.auth.SignInSession;
import com.official.lockr.domain.club.team.api.dto.AssignManagerTeamRequest;
import com.official.lockr.domain.club.team.api.dto.FoundTeamRequest;
import com.official.lockr.domain.club.team.application.AssignMangerUseCase;
import com.official.lockr.domain.club.team.application.FoundTeamUseCase;
import com.official.lockr.domain.club.team.domain.Team;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamApi {

    private final FoundTeamUseCase foundTeamUseCase;
    private final AssignMangerUseCase assignMangerUseCase;

    public TeamApi(final FoundTeamUseCase foundTeamUseCase,
                   final AssignMangerUseCase assignMangerUseCase
    ) {
        this.foundTeamUseCase = foundTeamUseCase;
        this.assignMangerUseCase = assignMangerUseCase;
    }

    @PostMapping
    public ResponseEntity<Team> found(
            final HttpSession httpSession,
            @RequestBody final FoundTeamRequest request
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        final Team team = foundTeamUseCase.found(request.toCommand(signIn.userId()));
        return ResponseEntity.created(URI.create("/api/v1/team/" + team.getId())).body(team);
    }

    @PostMapping("/{teamId}/manager")
    public ResponseEntity<Team> assignManager(
            final HttpSession httpSession,
            @PathVariable final String teamId,
            @RequestBody final AssignManagerTeamRequest request
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        return ResponseEntity.ok(assignMangerUseCase.assignManager(request.toCommand(teamId, signIn.userId())));
    }
}
