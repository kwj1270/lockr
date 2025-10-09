package com.official.lockr.domain.team.resume.api;

import com.official.lockr.domain.auth.domain.auth.SignInSession;
import com.official.lockr.domain.team.resume.api.dto.ApplyResumeRequest;
import com.official.lockr.domain.team.resume.api.dto.SignResumeRequest;
import com.official.lockr.domain.team.resume.application.ApplyResumeUseCase;
import com.official.lockr.domain.team.resume.application.SignResumeUseCase;
import com.official.lockr.domain.team.resume.domain.Contract;
import com.official.lockr.domain.team.resume.domain.Resume;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/teams/{teamId}/resume")
public class ResumeApi {

    private final ApplyResumeUseCase applyResumeUseCase;
    private final SignResumeUseCase signResumeUseCase;

    public ResumeApi(final ApplyResumeUseCase applyResumeUseCase, final SignResumeUseCase signResumeUseCase) {
        this.applyResumeUseCase = applyResumeUseCase;
        this.signResumeUseCase = signResumeUseCase;
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

    @PostMapping("/sign")
    public ResponseEntity<Contract> sign(
            @PathVariable final String teamId,
            @RequestBody final SignResumeRequest request,
            final HttpSession httpSession
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        final Contract contract = signResumeUseCase.sign(request.toCommand(teamId, signIn.userId()));
        return ResponseEntity.created(URI.create("/api/v1/team/" + teamId + "/contract/" + contract.getId())).body(contract);
    }
}
