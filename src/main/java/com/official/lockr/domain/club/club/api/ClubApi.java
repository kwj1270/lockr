package com.official.lockr.domain.club.club.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.club.api.dto.AssignCoachRequest;
import com.official.lockr.domain.club.club.api.dto.AssignManagerRequest;
import com.official.lockr.domain.club.club.api.dto.FoundClubRequest;
import com.official.lockr.domain.club.club.api.dto.UpdateMemberProfileImageRequest;
import com.official.lockr.domain.club.club.application.usecase.AssignCoachUseCase;
import com.official.lockr.domain.club.club.application.usecase.AssignMangerUseCase;
import com.official.lockr.domain.club.club.application.usecase.FoundClubUseCase;
import com.official.lockr.domain.club.club.application.usecase.UpdateMemberProfileImageUseCase;
import com.official.lockr.domain.club.club.domain.Club;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.net.URI;

import static java.util.Objects.isNull;

@RestController
@RequestMapping("/api/v1/clubs")
public class ClubApi {

    private final FoundClubUseCase foundClubUseCase;
    private final AssignMangerUseCase assignMangerUseCase;
    private final AssignCoachUseCase assignCoachUseCase;
    private final UpdateMemberProfileImageUseCase updateMemberProfileImageUseCase;

    public ClubApi(final FoundClubUseCase foundClubUseCase,
                   final AssignMangerUseCase assignMangerUseCase,
                   final AssignCoachUseCase assignCoachUseCase,
                   final UpdateMemberProfileImageUseCase updateMemberProfileImageUseCase
    ) {
        this.foundClubUseCase = foundClubUseCase;
        this.assignMangerUseCase = assignMangerUseCase;
        this.assignCoachUseCase = assignCoachUseCase;
        this.updateMemberProfileImageUseCase = updateMemberProfileImageUseCase;
    }

    @PostMapping
    public ResponseEntity<Club> found(
            final HttpSession httpSession,
            @RequestBody final FoundClubRequest request
    ) {
        final SignInSession signIn = session(httpSession);
        final Club club = foundClubUseCase.found(request.toCommand(signIn.userId()));
        return ResponseEntity.created(URI.create("/api/v1/clubs/" + club.getId())).body(club);
    }

    @PostMapping("/{clubId}/coach")
    public ResponseEntity<Club> assignCoach(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @RequestBody final AssignCoachRequest request
    ) {
        final SignInSession signIn = session(httpSession);
        return ResponseEntity.ok(assignCoachUseCase.assignCoach(request.toCommand(clubId, signIn.userId())));
    }

    @PostMapping("/{clubId}/manager")
    public ResponseEntity<Club> assignManager(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @RequestBody final AssignManagerRequest request
    ) {
        final SignInSession signIn = session(httpSession);
        return ResponseEntity.ok(assignMangerUseCase.assignManager(request.toCommand(clubId, signIn.userId())));
    }

    @PutMapping("/{clubId}/members/me/profile-image")
    public ResponseEntity<Club> updateMemberProfileImage(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @RequestBody final UpdateMemberProfileImageRequest request
    ) {
        final SignInSession signIn = session(httpSession);
        return ResponseEntity.ok(updateMemberProfileImageUseCase.updateMemberProfileImage(request.toCommand(clubId, signIn.userId())));
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(signIn)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return signIn;
    }
}
