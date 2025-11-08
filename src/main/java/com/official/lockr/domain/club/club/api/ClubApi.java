package com.official.lockr.domain.club.club.api;

import com.official.lockr.domain.auth.domain.signin.SignInSession;
import com.official.lockr.domain.club.club.api.dto.AssignManagerClubRequest;
import com.official.lockr.domain.club.club.api.dto.FoundClubRequest;
import com.official.lockr.domain.club.club.application.usecase.AssignMangerUseCase;
import com.official.lockr.domain.club.club.application.usecase.FoundClubUseCase;
import com.official.lockr.domain.club.club.domain.Club;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/clubs")
public class ClubApi {

    private final FoundClubUseCase foundClubUseCase;
    private final AssignMangerUseCase assignMangerUseCase;

    public ClubApi(final FoundClubUseCase foundClubUseCase,
                   final AssignMangerUseCase assignMangerUseCase
    ) {
        this.foundClubUseCase = foundClubUseCase;
        this.assignMangerUseCase = assignMangerUseCase;
    }

    @PostMapping
    public ResponseEntity<Club> found(
            final HttpSession httpSession,
            @RequestBody final FoundClubRequest request
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        final Club club = foundClubUseCase.found(request.toCommand(signIn.userId()));

        return ResponseEntity.created(URI.create("/api/v1/clubs/" + club.getId())).body(club);
    }

    @PostMapping("/{clubId}/manager")
    public ResponseEntity<Club> assignManager(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @RequestBody final AssignManagerClubRequest request
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        return ResponseEntity.ok(assignMangerUseCase.assignManager(request.toCommand(clubId, signIn.userId())));
    }
}
