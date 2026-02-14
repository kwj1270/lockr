package com.official.lockr.domain.club.sport.football.squad.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.sport.football.squad.api.dto.RegisterMySquadProfileRequest;
import com.official.lockr.domain.club.sport.football.squad.application.usecase.RegisterMySquadProfileUseCase;
import com.official.lockr.domain.club.sport.football.squad.domain.Squad;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/clubs/{clubId}/squads")
@RestController
public class SquadProfileApi {

    private final RegisterMySquadProfileUseCase registerMySquadProfileUseCase;

    public SquadProfileApi(final RegisterMySquadProfileUseCase registerMySquadProfileUseCase) {
        this.registerMySquadProfileUseCase = registerMySquadProfileUseCase;
    }

    @PostMapping("/me")
    public ResponseEntity<Squad> registerMyProfile(
            @PathVariable String clubId,
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @RequestBody RegisterMySquadProfileRequest request
    ) {
        final Squad squad = registerMySquadProfileUseCase.registerMyProfile(
                request.toCommand(signInSession.userId(), clubId)
        );
        return ResponseEntity.ok(squad);
    }
}
