package com.official.lockr.domain.shorts.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.shorts.api.dto.AddShortsCommentRequest;
import com.official.lockr.domain.shorts.api.dto.ReportShortsRequest;
import com.official.lockr.domain.shorts.api.dto.UploadShortsRequest;
import com.official.lockr.domain.shorts.application.command.DeleteShortsCommand;
import com.official.lockr.domain.shorts.application.command.DeleteShortsCommentCommand;
import com.official.lockr.domain.shorts.application.command.RestoreShortsCommand;
import com.official.lockr.domain.shorts.application.command.ToggleShortsHeartCommand;
import com.official.lockr.domain.shorts.application.usecase.*;
import com.official.lockr.domain.shorts.domain.Shorts;
import com.official.lockr.domain.shorts.domain.ShortsComment;
import com.official.lockr.domain.shorts.domain.ShortsReport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/shorts")
public class ShortsApi {

    private final UploadShortsUseCase uploadShortsUseCase;
    private final DeleteShortsUseCase deleteShortsUseCase;
    private final ToggleShortsHeartUseCase toggleShortsHeartUseCase;
    private final AddShortsCommentUseCase addShortsCommentUseCase;
    private final DeleteShortsCommentUseCase deleteShortsCommentUseCase;
    private final ReportShortsUseCase reportShortsUseCase;
    private final RestoreShortsUseCase restoreShortsUseCase;

    public ShortsApi(final UploadShortsUseCase uploadShortsUseCase,
                     final DeleteShortsUseCase deleteShortsUseCase,
                     final ToggleShortsHeartUseCase toggleShortsHeartUseCase,
                     final AddShortsCommentUseCase addShortsCommentUseCase,
                     final DeleteShortsCommentUseCase deleteShortsCommentUseCase,
                     final ReportShortsUseCase reportShortsUseCase,
                     final RestoreShortsUseCase restoreShortsUseCase) {
        this.uploadShortsUseCase = uploadShortsUseCase;
        this.deleteShortsUseCase = deleteShortsUseCase;
        this.toggleShortsHeartUseCase = toggleShortsHeartUseCase;
        this.addShortsCommentUseCase = addShortsCommentUseCase;
        this.deleteShortsCommentUseCase = deleteShortsCommentUseCase;
        this.reportShortsUseCase = reportShortsUseCase;
        this.restoreShortsUseCase = restoreShortsUseCase;
    }

    @PostMapping
    public ResponseEntity<Shorts> upload(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @RequestBody final UploadShortsRequest request
    ) {
        final Shorts shorts = uploadShortsUseCase.upload(request.toCommand(signInSession.userId()));
        return ResponseEntity.created(URI.create("/api/v1/shorts/" + shorts.getId())).body(shorts);
    }

    @PostMapping("/{shortsId}/delete")
    public ResponseEntity<Void> delete(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String shortsId,
            @RequestBody final DeleteShortsDeleteRequest request
    ) {
        deleteShortsUseCase.delete(new DeleteShortsCommand(shortsId, signInSession.userId(), request.clubId()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{shortsId}/hearts")
    public ResponseEntity<Shorts> toggleHeart(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String shortsId
    ) {
        final Shorts shorts = toggleShortsHeartUseCase.toggle(new ToggleShortsHeartCommand(shortsId, signInSession.userId()));
        return ResponseEntity.ok(shorts);
    }

    @PostMapping("/{shortsId}/comments")
    public ResponseEntity<ShortsComment> addComment(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String shortsId,
            @RequestBody final AddShortsCommentRequest request
    ) {
        final ShortsComment comment = addShortsCommentUseCase.add(request.toCommand(shortsId, signInSession.userId()));
        return ResponseEntity.created(URI.create("/api/v1/shorts/" + shortsId + "/comments/" + comment.getId())).body(comment);
    }

    @PostMapping("/{shortsId}/comments/{commentId}/delete")
    public ResponseEntity<Void> deleteComment(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String shortsId,
            @PathVariable final String commentId
    ) {
        deleteShortsCommentUseCase.delete(new DeleteShortsCommentCommand(shortsId, commentId, signInSession.userId()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{shortsId}/reports")
    public ResponseEntity<ShortsReport> report(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String shortsId,
            @RequestBody final ReportShortsRequest request
    ) {
        final ShortsReport report = reportShortsUseCase.report(request.toCommand(shortsId, signInSession.userId()));
        return ResponseEntity.created(URI.create("/api/v1/shorts/" + shortsId + "/reports/" + report.getId())).body(report);
    }

    @PostMapping("/{shortsId}/restore")
    public ResponseEntity<Void> restore(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String shortsId,
            @RequestBody final RestoreShortsRequest request
    ) {
        restoreShortsUseCase.restore(new RestoreShortsCommand(shortsId, signInSession.userId(), request.clubId()));
        return ResponseEntity.ok().build();
    }

    public record DeleteShortsDeleteRequest(String clubId) {
    }

    public record RestoreShortsRequest(String clubId) {
    }
}
