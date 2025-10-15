package com.official.lockr.domain.club.contract.api;

import com.official.lockr.domain.auth.domain.auth.SignInSession;
import com.official.lockr.domain.club.contract.api.dto.ApplyResumeRequest;
import com.official.lockr.domain.club.contract.application.ApplyResumeUseCase;
import com.official.lockr.domain.club.contract.application.CancelResumeUseCase;
import com.official.lockr.domain.club.contract.application.command.CancelResumeCommand;
import com.official.lockr.domain.club.contract.domain.Resume;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/teams/{teamId}/resume")
public class ResumeApi {

    private final ApplyResumeUseCase applyResumeUseCase;
    private final CancelResumeUseCase cancelResumeUseCase;

    public ResumeApi(final ApplyResumeUseCase applyResumeUseCase,
                     final CancelResumeUseCase cancelResumeUseCase
    ) {
        this.applyResumeUseCase = applyResumeUseCase;
        this.cancelResumeUseCase = cancelResumeUseCase;
    }

    @PostMapping
    public ResponseEntity<Resume> apply(
            @PathVariable("teamId") final String teamId,
            @RequestBody final ApplyResumeRequest request,
            final HttpSession httpSession
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        final Resume resume = applyResumeUseCase.apply(request.toCommand(teamId, signIn.userId()));
        return ResponseEntity.created(URI.create("/api/v1/team/" + teamId + "/" + resume.getId())).body(resume);
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable("teamId") final String teamId,
            final HttpSession httpSession
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        cancelResumeUseCase.cancel(new CancelResumeCommand(teamId, signIn.userId()));
        return ResponseEntity.ok().build();
    }
}
