package com.official.lockr.domain.club.sqaud.api;

import com.official.lockr.domain.auth.domain.auth.SignInSession;
import com.official.lockr.domain.club.sqaud.domain.Squad;
import com.official.lockr.domain.club.sqaud.domain.SquadRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Objects.isNull;

@RequestMapping("/api/v1/clubs/{clubId}/squads")
@RestController
public class SquadApi {

    private final SquadRepository squadRepository;

    public SquadApi(final SquadRepository squadRepository) {
        this.squadRepository = squadRepository;
    }

    @GetMapping
    public ResponseEntity<Squad> find(
            @PathVariable String clubId,
            final HttpSession httpSession
    ) {
        final SignInSession signInSession = session(httpSession);
        final Squad squad = squadRepository.findByClubId(clubId);
        return ResponseEntity.ok(squad);
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(signIn)) {
            throw new IllegalStateException("User not authenticated");
        }
        return signIn;
    }
}
