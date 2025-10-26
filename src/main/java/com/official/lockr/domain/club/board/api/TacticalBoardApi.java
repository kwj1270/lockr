package com.official.lockr.domain.club.board.api;

import com.official.lockr.domain.auth.domain.auth.SignInSession;
import com.official.lockr.domain.club.board.api.dto.*;
import com.official.lockr.domain.club.board.application.usecase.*;
import com.official.lockr.domain.club.board.domain.TacticalBoard;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static java.util.Objects.isNull;

@RequestMapping("/api/v1/clubs/{clubId}/boards")
@RestController
public class TacticalBoardApi {

    private final CreateTacticalBoardUseCase createTacticalBoardUseCase;
    private final AddPlayerTacticalBoardUseCase addPlayerTacticalBoardUseCase;
    private final MoveLocationTacticalBoardUseCase moveLocationTacticalBoardUseCase;
    private final SubstitutePlayerUseCase substituteEntryUseCase;
    private final DeleteTacticalBoardUseCase deleteTacticalBoardUseCase;
    private final ApplyFormationTacticalBoardUseCase applyFormationTacticalBoardUseCase;

    public TacticalBoardApi(final CreateTacticalBoardUseCase createTacticalBoardUseCase,
                            final AddPlayerTacticalBoardUseCase addPlayerTacticalBoardUseCase,
                            final MoveLocationTacticalBoardUseCase moveLocationTacticalBoardUseCase,
                            final SubstitutePlayerUseCase substituteEntryUseCase,
                            final DeleteTacticalBoardUseCase deleteTacticalBoardUseCase,
                            final ApplyFormationTacticalBoardUseCase applyFormationTacticalBoardUseCase) {
        this.createTacticalBoardUseCase = createTacticalBoardUseCase;
        this.addPlayerTacticalBoardUseCase = addPlayerTacticalBoardUseCase;
        this.moveLocationTacticalBoardUseCase = moveLocationTacticalBoardUseCase;
        this.substituteEntryUseCase = substituteEntryUseCase;
        this.deleteTacticalBoardUseCase = deleteTacticalBoardUseCase;
        this.applyFormationTacticalBoardUseCase = applyFormationTacticalBoardUseCase;
    }

    @PostMapping
    public ResponseEntity<TacticalBoard> create(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @RequestBody final CreateTacticalBoardRequest request
    ) {
        final SignInSession session = session(httpSession);
        final TacticalBoard tacticalBoard = createTacticalBoardUseCase.create(
                request.toCommand(clubId, session.userId())
        );
        return ResponseEntity.ok(tacticalBoard);
    }

    @PostMapping("/{tacticalBoardId}/player")
    public ResponseEntity<TacticalBoard> addPlayer(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String tacticalBoardId,
            @RequestBody final AddPlayerTacticalBoardRequest request
    ) {
        final SignInSession session = session(httpSession);
        final TacticalBoard tacticalBoard = addPlayerTacticalBoardUseCase.addPlayer(
                request.toCommand(clubId, tacticalBoardId, session.userId())
        );
        return ResponseEntity.ok(tacticalBoard);
    }

    @PostMapping("/{tacticalBoardId}/move/location")
    public ResponseEntity<TacticalBoard> moveLocation(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String tacticalBoardId,
            @RequestBody final MoveLocationTacticalBoardRequest request
    ) {
        final SignInSession session = session(httpSession);
        final TacticalBoard tacticalBoard = moveLocationTacticalBoardUseCase.moveLocation(
                request.toCommand(clubId, tacticalBoardId, session.userId())
        );
        return ResponseEntity.ok(tacticalBoard);
    }

    @PostMapping("/{tacticalBoardId}/formation")
    public ResponseEntity<TacticalBoard> applyFormation(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String tacticalBoardId,
            @RequestBody final ApplyFormationTacticalBoardRequest request
    ) {
        final SignInSession session = session(httpSession);
        final TacticalBoard tacticalBoard = applyFormationTacticalBoardUseCase.applyFormation(
                request.toCommand(clubId, tacticalBoardId, session.userId())
        );
        return ResponseEntity.ok(tacticalBoard);
    }

    @PostMapping("/{tacticalBoardId}/substitute")
    public ResponseEntity<TacticalBoard> substitute(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String tacticalBoardId,
            @RequestBody final SubstitutePlayerRequest request
    ) {
        final SignInSession session = session(httpSession);
        final TacticalBoard tacticalBoard = substituteEntryUseCase.substitute(
                request.toCommand(clubId, tacticalBoardId, session.userId())
        );
        return ResponseEntity.ok(tacticalBoard);
    }

    @DeleteMapping("/{tacticalBoardId}")
    public void delete(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String tacticalBoardId
    ) {
        final SignInSession session = session(httpSession);
        deleteTacticalBoardUseCase.delete(clubId, tacticalBoardId, session.userId());
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(signIn)) {
            throw new IllegalStateException("User not authenticated");
        }
        return signIn;
    }
}
