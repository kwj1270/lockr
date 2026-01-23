package com.official.lockr.domain.club.recruitment.recruitment.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.recruitment.recruitment.api.dto.PostRecruitmentRequest;
import com.official.lockr.domain.club.recruitment.recruitment.api.dto.UpdateRecruitmentRequest;
import com.official.lockr.domain.club.recruitment.recruitment.application.usecase.PostRecruitmentUseCase;
import com.official.lockr.domain.club.recruitment.recruitment.application.usecase.UpdateRecruitmentUseCase;
import com.official.lockr.domain.club.recruitment.recruitment.domain.Recruitment;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.net.URI;

import static java.util.Objects.isNull;

@RestController
@RequestMapping("/api/v1/clubs/{clubId}/recruitments")
public class RecruitmentApi {

    private final PostRecruitmentUseCase postRecruitmentUseCase;
    private final UpdateRecruitmentUseCase updateRecruitmentUseCase;

    public RecruitmentApi(final PostRecruitmentUseCase postRecruitmentUseCase,
                          final UpdateRecruitmentUseCase updateRecruitmentUseCase) {
        this.postRecruitmentUseCase = postRecruitmentUseCase;
        this.updateRecruitmentUseCase = updateRecruitmentUseCase;
    }

    @PostMapping
    public ResponseEntity<Recruitment> post(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @RequestBody final PostRecruitmentRequest request
    ) {
        final SignInSession signIn = session(httpSession);
        final Recruitment recruitment = postRecruitmentUseCase.post(request.toCommand(clubId, signIn.userId()));
        return ResponseEntity.created(URI.create("/api/v1/recruitments/" + recruitment.getId())).body(recruitment);
    }

    @PostMapping("/{recruitmentId}/update")
    public ResponseEntity<Recruitment> update(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String recruitmentId,
            @RequestBody final UpdateRecruitmentRequest request
    ) {
        final SignInSession signIn = session(httpSession);
        final Recruitment recruitment = updateRecruitmentUseCase.update(request.toCommand(clubId, recruitmentId, signIn.userId()));
        return ResponseEntity.ok(recruitment);
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(signIn)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return signIn;
    }
}
