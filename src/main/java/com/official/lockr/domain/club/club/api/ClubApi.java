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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @RequestBody final FoundClubRequest request
    ) {
        final Club club = foundClubUseCase.found(request.toCommand(signInSession.userId()));
        return ResponseEntity.created(URI.create("/api/v1/clubs/" + club.getId())).body(club);
    }

    @PostMapping("/{clubId}/coach")
    public ResponseEntity<Club> assignCoach(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @RequestBody final AssignCoachRequest request
    ) {
        return ResponseEntity.ok(assignCoachUseCase.assignCoach(request.toCommand(clubId, signInSession.userId())));
    }

    @PostMapping("/{clubId}/manager")
    public ResponseEntity<Club> assignManager(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @RequestBody final AssignManagerRequest request
    ) {
        return ResponseEntity.ok(assignMangerUseCase.assignManager(request.toCommand(clubId, signInSession.userId())));
    }

    @PostMapping("/{clubId}/members/me/profile-image")
    public ResponseEntity<Club> updateMemberProfileImage(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @RequestBody final UpdateMemberProfileImageRequest request
    ) {
        return ResponseEntity.ok(updateMemberProfileImageUseCase.updateMemberProfileImage(request.toCommand(clubId, signInSession.userId())));
    }
}
