package com.official.lockr.domain.club.sqaud.api;

import com.official.lockr.domain.auth.domain.auth.SignInSession;
import com.official.lockr.domain.club.sqaud.api.dto.AddMatchPlayerTacticalBoardRequest;
import com.official.lockr.domain.club.sqaud.api.dto.ApplyFormationTacticalBoardRequest;
import com.official.lockr.domain.club.sqaud.api.dto.CreateTacticalBoardRequest;
import com.official.lockr.domain.club.sqaud.api.dto.SubstitutePlayerRequest;
import com.official.lockr.domain.club.sqaud.application.usecase.*;
import com.official.lockr.domain.club.sqaud.domain.board.TacticalBoard;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static java.util.Objects.isNull;

@RequestMapping("/api/v1/squads/{squadId}/boards")
@RestController
public class TacticalBoardApi {

    private final CreateTacticalBoardUseCase createTacticalBoardUseCase;
    private final AddMatchPlayerTacticalBoardUseCase addMatchPlayerTacticalBoardUseCase;
    private final MovePositionTacticalBoardUseCase movePositionTacticalBoardUseCase;
    private final SubstituteMatchPlayerUseCase substituteEntryUseCase;
    private final DeleteTacticalBoardUseCase deleteTacticalBoardUseCase;
    private final ApplyFormationTacticalBoardUseCase applyFormationTacticalBoardUseCase;

    public TacticalBoardApi(final CreateTacticalBoardUseCase createTacticalBoardUseCase,
                            final AddMatchPlayerTacticalBoardUseCase addMatchPlayerTacticalBoardUseCase,
                            final MovePositionTacticalBoardUseCase movePositionTacticalBoardUseCase,
                            final SubstituteMatchPlayerUseCase substituteEntryUseCase,
                            final DeleteTacticalBoardUseCase deleteTacticalBoardUseCase,
                            final ApplyFormationTacticalBoardUseCase applyFormationTacticalBoardUseCase) {
        this.createTacticalBoardUseCase = createTacticalBoardUseCase;
        this.addMatchPlayerTacticalBoardUseCase = addMatchPlayerTacticalBoardUseCase;
        this.movePositionTacticalBoardUseCase = movePositionTacticalBoardUseCase;
        this.substituteEntryUseCase = substituteEntryUseCase;
        this.deleteTacticalBoardUseCase = deleteTacticalBoardUseCase;
        this.applyFormationTacticalBoardUseCase = applyFormationTacticalBoardUseCase;
    }

    @PostMapping
    public ResponseEntity<TacticalBoard> create(
            final HttpSession httpSession,
            @PathVariable final String squadId,
            @RequestBody final CreateTacticalBoardRequest request
    ) {
        final SignInSession session = session(httpSession);
        final TacticalBoard tacticalBoard = createTacticalBoardUseCase.create(request.toCommand(session.userId(), squadId));
        return ResponseEntity.ok(tacticalBoard);
    }

    @PostMapping("/{tacticalBoardId}/player")
    public ResponseEntity<TacticalBoard> addMatchPlayer(
            final HttpSession httpSession,
            @PathVariable final String squadId,
            @PathVariable final String tacticalBoardId,
            @RequestBody final AddMatchPlayerTacticalBoardRequest request
    ) {
        final SignInSession session = session(httpSession);
        final TacticalBoard tacticalBoard = addMatchPlayerTacticalBoardUseCase.addMatchPlayer(
                request.toCommand(session.userId(), squadId, tacticalBoardId)
        );
        return ResponseEntity.ok(tacticalBoard);
    }

    @PostMapping("/{tacticalBoardId}/formation")
    public ResponseEntity<TacticalBoard> applyFormation(
            final HttpSession httpSession,
            @PathVariable final String squadId,
            @PathVariable final String tacticalBoardId,
            @RequestBody final ApplyFormationTacticalBoardRequest request
    ) {
        final SignInSession session = session(httpSession);
        final TacticalBoard tacticalBoard = applyFormationTacticalBoardUseCase.applyFormation(
                request.toCommand(session.userId(), squadId, tacticalBoardId)
        );
        return ResponseEntity.ok(tacticalBoard);
    }

    @PostMapping("/{tacticalBoardId}/substitute")
    public ResponseEntity<TacticalBoard> substitute(
            final HttpSession httpSession,
            @PathVariable final String squadId,
            @PathVariable final String tacticalBoardId,
            @RequestBody final SubstitutePlayerRequest request
    ) {
        final SignInSession session = session(httpSession);
        final TacticalBoard tacticalBoard = substituteEntryUseCase.substitute(
                request.toCommand(session.userId(), squadId, tacticalBoardId)
        );
        return ResponseEntity.ok(tacticalBoard);
    }

    @DeleteMapping("/{tacticalBoardId}")
    public void delete(
            final HttpSession httpSession,
            @PathVariable final String squadId,
            @PathVariable final String tacticalBoardId
    ) {
        final SignInSession session = session(httpSession);
        deleteTacticalBoardUseCase.delete(squadId, session.userId(), tacticalBoardId);
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(signIn)) {
            throw new IllegalStateException("User not authenticated");
        }
        return signIn;
    }
}
