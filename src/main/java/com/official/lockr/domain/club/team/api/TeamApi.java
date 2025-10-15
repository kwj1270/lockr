package com.official.lockr.domain.club.team.api;

import com.official.lockr.domain.auth.domain.auth.SignInSession;
import com.official.lockr.domain.club.team.api.dto.FoundTeamRequest;
import com.official.lockr.domain.club.team.application.FoundTeamUseCase;
import com.official.lockr.domain.club.team.domain.Team;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamApi {

    private final FoundTeamUseCase foundTeamUseCase;

    public TeamApi(final FoundTeamUseCase foundTeamUseCase) {
        this.foundTeamUseCase = foundTeamUseCase;
    }

    @PostMapping
    public ResponseEntity<Team> found(
            @RequestBody final FoundTeamRequest request,
            final HttpSession httpSession
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        final Team team = foundTeamUseCase.found(request.toCommand(signIn.userId()));
        return ResponseEntity.created(URI.create("/api/v1/team/" + team.getId())).body(team);
    }
}
