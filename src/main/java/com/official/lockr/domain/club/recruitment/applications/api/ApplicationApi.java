package com.official.lockr.domain.club.recruitment.applications.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.recruitment.applications.domain.Application;
import com.official.lockr.domain.club.recruitment.applications.api.dto.RejectApplicationRequest;
import com.official.lockr.domain.club.recruitment.applications.api.dto.SubmitApplicationRequest;
import com.official.lockr.domain.club.recruitment.applications.application.usecase.ApproveApplicationUseCase;
import com.official.lockr.domain.club.recruitment.applications.application.usecase.CancelApplicationUseCase;
import com.official.lockr.domain.club.recruitment.applications.application.usecase.RejectApplicationUseCase;
import com.official.lockr.domain.club.recruitment.applications.application.usecase.SubmitApplicationUseCase;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/clubs/{clubId}/applications")
public class ApplicationApi {

    private final SubmitApplicationUseCase submitApplicationUseCase;
    private final CancelApplicationUseCase cancelApplicationUseCase;
    private final ApproveApplicationUseCase approveApplicationUseCase;
    private final RejectApplicationUseCase rejectApplicationUseCase;

    public ApplicationApi(
            final SubmitApplicationUseCase submitApplicationUseCase,
            final CancelApplicationUseCase cancelApplicationUseCase,
            final ApproveApplicationUseCase approveApplicationUseCase,
            final RejectApplicationUseCase rejectApplicationUseCase
    ) {
        this.submitApplicationUseCase = submitApplicationUseCase;
        this.cancelApplicationUseCase = cancelApplicationUseCase;
        this.approveApplicationUseCase = approveApplicationUseCase;
        this.rejectApplicationUseCase = rejectApplicationUseCase;
    }

    @PostMapping
    public ResponseEntity<Application> submit(
            @PathVariable("clubId") final String clubId,
            @RequestBody final SubmitApplicationRequest request,
            final HttpSession httpSession
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        final Application application = submitApplicationUseCase.submit(request.toCommand(clubId, signIn.userId()));
        return ResponseEntity.created(URI.create("/api/v1/clubs/" + clubId + "/applications/" + application.getId())).body(application);
    }

    @PostMapping("/{applicationId}/cancel")
    public ResponseEntity<Application> cancel(
            final HttpSession httpSession,
            @PathVariable("clubId") final String clubId,
            @PathVariable("applicationId") final String tryoutId
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        final Application application = cancelApplicationUseCase.cancel(clubId, tryoutId, signIn.userId());
        return ResponseEntity.ok().body(application);
    }

    @PostMapping("/{applicationId}/approve")
    public ResponseEntity<Application> approve(
            final HttpSession httpSession,
            @PathVariable("clubId") final String clubId,
            @PathVariable("applicationId") final String applicationId
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        final Application application = approveApplicationUseCase.approve(clubId, applicationId, signIn.userId());
        return ResponseEntity.ok().body(application);
    }

    @PostMapping("/{applicationId}/reject")
    public ResponseEntity<Application> reject(
            final HttpSession httpSession,
            @PathVariable("clubId") final String clubId,
            @PathVariable("applicationId") final String applicationId,
            @RequestBody final RejectApplicationRequest request
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        final Application application = rejectApplicationUseCase.reject(request.toCommand(clubId, applicationId, signIn.userId()));
        return ResponseEntity.ok().body(application);
    }
}
