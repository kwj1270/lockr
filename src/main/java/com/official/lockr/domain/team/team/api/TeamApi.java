package com.official.lockr.domain.team.team.api;

import com.official.lockr.domain.auth.domain.auth.SignInSession;
import com.official.lockr.domain.team.team.application.RegisterTeamUseCase;
import com.official.lockr.domain.team.team.domain.Team;
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

    private final RegisterTeamUseCase registerTeamUseCase;

    public TeamApi(final RegisterTeamUseCase registerTeamUseCase) {
        this.registerTeamUseCase = registerTeamUseCase;
    }

    @PostMapping
    public ResponseEntity<Team> register(
            @RequestBody final RegisterTeamRequest request,
            final HttpSession httpSession
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        final Team team = registerTeamUseCase.register(request.toCommand(signIn.userId()));
        return ResponseEntity.created(URI.create("/api/v1/team/" + team.getId())).body(team);
    }
}
