package com.official.lockr.domain.club.recruitment.recruitment.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.recruitment.recruitment.api.dto.ChangeRecruitmentStatusRequest;
import com.official.lockr.domain.club.recruitment.recruitment.api.dto.PostRecruitmentRequest;
import com.official.lockr.domain.club.recruitment.recruitment.api.dto.UpdateRecruitmentRequest;
import com.official.lockr.domain.club.recruitment.recruitment.application.usecase.ChangeRecruitmentStatusUseCase;
import com.official.lockr.domain.club.recruitment.recruitment.application.usecase.DeleteRecruitmentUseCase;
import com.official.lockr.domain.club.recruitment.recruitment.application.usecase.PostRecruitmentUseCase;
import com.official.lockr.domain.club.recruitment.recruitment.application.usecase.UpdateRecruitmentUseCase;
import com.official.lockr.domain.club.recruitment.recruitment.domain.Recruitment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/clubs/{clubId}/recruitments")
public class RecruitmentApi {

    private final PostRecruitmentUseCase postRecruitmentUseCase;
    private final UpdateRecruitmentUseCase updateRecruitmentUseCase;
    private final DeleteRecruitmentUseCase deleteRecruitmentUseCase;
    private final ChangeRecruitmentStatusUseCase changeRecruitmentStatusUseCase;

    public RecruitmentApi(final PostRecruitmentUseCase postRecruitmentUseCase,
                          final UpdateRecruitmentUseCase updateRecruitmentUseCase,
                          final DeleteRecruitmentUseCase deleteRecruitmentUseCase,
                          final ChangeRecruitmentStatusUseCase changeRecruitmentStatusUseCase) {
        this.postRecruitmentUseCase = postRecruitmentUseCase;
        this.updateRecruitmentUseCase = updateRecruitmentUseCase;
        this.deleteRecruitmentUseCase = deleteRecruitmentUseCase;
        this.changeRecruitmentStatusUseCase = changeRecruitmentStatusUseCase;
    }

    @PostMapping
    public ResponseEntity<Recruitment> post(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @RequestBody final PostRecruitmentRequest request
    ) {
        final Recruitment recruitment = postRecruitmentUseCase.post(request.toCommand(clubId, signInSession.userId()));
        return ResponseEntity.created(URI.create("/api/v1/recruitments/" + recruitment.getId())).body(recruitment);
    }

    @PostMapping("/{recruitmentId}/update")
    public ResponseEntity<Recruitment> update(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String recruitmentId,
            @RequestBody final UpdateRecruitmentRequest request
    ) {
        final Recruitment recruitment = updateRecruitmentUseCase.update(request.toCommand(clubId, recruitmentId, signInSession.userId()));
        return ResponseEntity.ok(recruitment);
    }

    @PostMapping("/{recruitmentId}/delete")
    public ResponseEntity<Void> delete(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String recruitmentId
    ) {
        deleteRecruitmentUseCase.delete(clubId, recruitmentId, signInSession.userId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{recruitmentId}/status")
    public ResponseEntity<Recruitment> changeStatus(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String recruitmentId,
            @RequestBody final ChangeRecruitmentStatusRequest request
    ) {
        final Recruitment recruitment = changeRecruitmentStatusUseCase.changeStatus(clubId, recruitmentId, request.status(), signInSession.userId());
        return ResponseEntity.ok(recruitment);
    }
}
