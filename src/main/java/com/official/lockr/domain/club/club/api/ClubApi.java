package com.official.lockr.domain.club.club.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.club.api.dto.*;
import com.official.lockr.domain.club.club.application.command.AddMemberCommand;
import com.official.lockr.domain.club.club.application.command.KickClubMemberCommand;
import com.official.lockr.domain.club.club.application.command.LeaveClubCommand;
import com.official.lockr.domain.club.club.application.usecase.*;
import com.official.lockr.domain.club.club.domain.Club;
import com.official.lockr.domain.club.club.domain.MemberRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/clubs")
public class ClubApi {

    private final FoundClubUseCase foundClubUseCase;
    private final RegisterClubMemberUseCase registerClubMemberUseCase;
    private final AssignMangerUseCase assignMangerUseCase;
    private final AssignCoachUseCase assignCoachUseCase;
    private final UpdateMemberProfileImageUseCase updateMemberProfileImageUseCase;
    private final DelegatePresidentUseCase delegatePresidentUseCase;
    private final ChangeMemberRoleUseCase changeMemberRoleUseCase;
    private final ChangeVisibilityUseCase changeVisibilityUseCase;
    private final ChangeJoinMethodUseCase changeJoinMethodUseCase;
    private final LeaveClubUseCase leaveClubUseCase;
    private final UpdateClubUseCase updateClubUseCase;
    private final KickClubMemberUseCase kickClubMemberUseCase;

    public ClubApi(final FoundClubUseCase foundClubUseCase,
                   final RegisterClubMemberUseCase registerClubMemberUseCase,
                   final AssignMangerUseCase assignMangerUseCase,
                   final AssignCoachUseCase assignCoachUseCase,
                   final UpdateMemberProfileImageUseCase updateMemberProfileImageUseCase,
                   final DelegatePresidentUseCase delegatePresidentUseCase,
                   final ChangeMemberRoleUseCase changeMemberRoleUseCase,
                   final ChangeVisibilityUseCase changeVisibilityUseCase,
                   final ChangeJoinMethodUseCase changeJoinMethodUseCase,
                   final LeaveClubUseCase leaveClubUseCase,
                   final UpdateClubUseCase updateClubUseCase,
                   final KickClubMemberUseCase kickClubMemberUseCase
    ) {
        this.foundClubUseCase = foundClubUseCase;
        this.registerClubMemberUseCase = registerClubMemberUseCase;
        this.assignMangerUseCase = assignMangerUseCase;
        this.assignCoachUseCase = assignCoachUseCase;
        this.updateMemberProfileImageUseCase = updateMemberProfileImageUseCase;
        this.delegatePresidentUseCase = delegatePresidentUseCase;
        this.changeMemberRoleUseCase = changeMemberRoleUseCase;
        this.changeVisibilityUseCase = changeVisibilityUseCase;
        this.changeJoinMethodUseCase = changeJoinMethodUseCase;
        this.leaveClubUseCase = leaveClubUseCase;
        this.updateClubUseCase = updateClubUseCase;
        this.kickClubMemberUseCase = kickClubMemberUseCase;
    }

    @PostMapping
    public ResponseEntity<Club> found(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @RequestBody final FoundClubRequest request
    ) {
        final Club club = foundClubUseCase.found(request.toCommand(signInSession.userId()));
        registerClubMemberUseCase.addMember(AddMemberCommand.president(club.getId(), signInSession.userId(), null, null));
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

    @PostMapping("/{clubId}/me/profile-image")
    public ResponseEntity<Club> updateMemberProfileImage(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @RequestBody final UpdateMemberProfileImageRequest request
    ) {
        return ResponseEntity.ok(updateMemberProfileImageUseCase.updateMemberProfileImage(request.toCommand(clubId, signInSession.userId())));
    }

    @PostMapping("/{clubId}/delegate-president")
    public ResponseEntity<Club> delegatePresident(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @RequestBody final DelegatePresidentRequest request
    ) {
        return ResponseEntity.ok(delegatePresidentUseCase.delegatePresident(request.toCommand(clubId, signInSession.userId())));
    }

    @PostMapping("/{clubId}/members/{memberId}/role")
    public ResponseEntity<Club> changeMemberRole(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String memberId,
            @RequestBody final ChangeMemberRoleRequest request
    ) {
        return ResponseEntity.ok(changeMemberRoleUseCase.changeMemberRole(request.toCommand(clubId, signInSession.userId(), memberId)));
    }

    @PostMapping("/{clubId}/visibility")
    public ResponseEntity<Club> changeVisibility(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @RequestBody final ChangeVisibilityRequest request
    ) {
        return ResponseEntity.ok(changeVisibilityUseCase.changeVisibility(request.toCommand(clubId, signInSession.userId())));
    }

    @PostMapping("/{clubId}/join-method")
    public ResponseEntity<Club> changeJoinMethod(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @RequestBody final ChangeJoinMethodRequest request
    ) {
        return ResponseEntity.ok(changeJoinMethodUseCase.changeJoinMethod(request.toCommand(clubId, signInSession.userId())));
    }

    @PostMapping("/{clubId}")
    public ResponseEntity<Club> update(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @RequestBody final UpdateClubRequest request
    ) {
        return ResponseEntity.ok(updateClubUseCase.update(request.toCommand(clubId, signInSession.userId())));
    }

    @PostMapping("/{clubId}/members/{memberId}/remove")
    public ResponseEntity<Void> kickMember(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String memberId
    ) {
        kickClubMemberUseCase.kick(new KickClubMemberCommand(clubId, signInSession.userId(), memberId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{clubId}/leave")
    public ResponseEntity<Void> leave(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId
    ) {
        leaveClubUseCase.leave(new LeaveClubCommand(clubId, signInSession.userId()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{clubId}/images")
    public ResponseEntity<Map<String, String>> uploadClubImage(
            @PathVariable("clubId") final String clubId,
            @RequestParam("file") final MultipartFile file,
            @RequestAttribute("signInSession") final SignInSession signInSession
    ) {
        // TODO: implement file storage (local/S3)
        String imageUrl = "/images/clubs/" + clubId + "/" + file.getOriginalFilename();
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    @PostMapping("/{clubId}/delete")
    public ResponseEntity<Void> delete(
            @PathVariable("clubId") final String clubId,
            @RequestAttribute("signInSession") final SignInSession signInSession
    ) {
        // TODO: implement club deletion (soft delete, president only)
        return ResponseEntity.ok().build();
    }
}
