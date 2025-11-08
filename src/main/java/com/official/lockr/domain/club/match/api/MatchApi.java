package com.official.lockr.domain.club.match.api;

import com.official.lockr.domain.auth.domain.signin.SignInSession;
import com.official.lockr.domain.club.match.api.dto.AcceptMatchRequest;
import com.official.lockr.domain.club.match.api.dto.InviteMatchRequest;
import com.official.lockr.domain.club.match.application.AcceptMatchUseCase;
import com.official.lockr.domain.club.match.application.InviteMatchUseCase;
import com.official.lockr.domain.club.match.domain.Match;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clubs/{clubId}/matches")
public class MatchApi {

    private final InviteMatchUseCase inviteMatchUseCase;
    private final AcceptMatchUseCase acceptMatchUseCase;

    public MatchApi(final InviteMatchUseCase inviteMatchUseCase, final AcceptMatchUseCase acceptMatchUseCase) {
        this.inviteMatchUseCase = inviteMatchUseCase;
        this.acceptMatchUseCase = acceptMatchUseCase;
    }

    @PostMapping("/invite")
    public ResponseEntity<Match> invite(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @RequestBody final InviteMatchRequest request
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        return ResponseEntity.ok(inviteMatchUseCase.invite(request.toCommand(clubId, signIn.userId())));
    }

    @PostMapping("/{matchId}")
    public ResponseEntity<Match> accept(
            final HttpSession httpSession,
            @PathVariable final String matchId,
            @RequestBody final AcceptMatchRequest request
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        return ResponseEntity.ok(acceptMatchUseCase.accept(request.toCommand(matchId, signIn.userId())));
    }
}
