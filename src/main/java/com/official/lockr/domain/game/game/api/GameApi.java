package com.official.lockr.domain.game.game.api;

import com.official.lockr.domain.auth.domain.signin.SignInSession;
import com.official.lockr.domain.game.game.api.dto.FinishGameRequest;
import com.official.lockr.domain.game.game.api.dto.SubmitEntryGameTeamRequest;
import com.official.lockr.domain.game.game.application.FinishGameUseCase;
import com.official.lockr.domain.game.game.application.StartGameUseCase;
import com.official.lockr.domain.game.game.application.SubmitEntryGameUseCase;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import static java.util.Objects.isNull;

@RequestMapping("/api/v1/game/{gameId}")
@RestController
public class GameApi {

    private final SubmitEntryGameUseCase submitEntryGameUseCase;
    private final StartGameUseCase startGameUseCase;
    private final FinishGameUseCase finishGameUseCase;

    public GameApi(final SubmitEntryGameUseCase submitEntryGameUseCase,
                   final StartGameUseCase startGameUseCase,
                   final FinishGameUseCase finishGameUseCase
    ) {
        this.submitEntryGameUseCase = submitEntryGameUseCase;
        this.startGameUseCase = startGameUseCase;
        this.finishGameUseCase = finishGameUseCase;
    }

    @PostMapping("/team/{teamId}/entry")
    public void submitEntry(
            final HttpSession httpSession,
            @PathVariable final String gameId,
            @PathVariable final String teamId,
            @RequestBody final SubmitEntryGameTeamRequest request
    ) {
        final SignInSession signIn = session(httpSession);
        submitEntryGameUseCase.registerEntry(request.toCommand(gameId, teamId, signIn.userId()));
    }

    @PostMapping("/start")
    public void start(final HttpSession httpSession, @PathVariable final String gameId) {
        session(httpSession);
        startGameUseCase.start(gameId);
    }

    @PostMapping("/finish")
    public void finish(
            final HttpSession httpSession,
            @PathVariable final String gameId,
            @RequestBody final FinishGameRequest request
    ) {
        session(httpSession);
        finishGameUseCase.finish(gameId, request.totalMinutes());
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(signIn)) {
            throw new IllegalStateException();
        }
        return signIn;
    }
}
