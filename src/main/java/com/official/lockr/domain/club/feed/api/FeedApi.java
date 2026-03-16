package com.official.lockr.domain.club.feed.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.feed.api.dto.*;
import com.official.lockr.domain.club.feed.application.command.*;
import com.official.lockr.domain.club.feed.application.usecase.*;
import com.official.lockr.domain.club.feed.domain.Feed;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/clubs/{clubId}/feeds")
@RestController
public class FeedApi {

    private final CreateFeedUseCase createFeedUseCase;
    private final UpdateFeedUseCase updateFeedUseCase;
    private final DeleteFeedUseCase deleteFeedUseCase;
    private final AddCommentUseCase addCommentUseCase;
    private final UpdateCommentUseCase updateCommentUseCase;
    private final AddCommentHeartUseCase addCommentHeartUseCase;
    private final RemoveCommentHeartUseCase removeCommentHeartUseCase;
    private final AddHeartUseCase addHeartUseCase;
    private final RemoveHeartUseCase removeHeartUseCase;
    private final ReportFeedUseCase reportFeedUseCase;

    public FeedApi(
            final CreateFeedUseCase createFeedUseCase,
            final UpdateFeedUseCase updateFeedUseCase,
            final DeleteFeedUseCase deleteFeedUseCase,
            final AddCommentUseCase addCommentUseCase,
            final UpdateCommentUseCase updateCommentUseCase,
            final AddCommentHeartUseCase addCommentHeartUseCase,
            final RemoveCommentHeartUseCase removeCommentHeartUseCase,
            final AddHeartUseCase addHeartUseCase,
            final RemoveHeartUseCase removeHeartUseCase,
            final ReportFeedUseCase reportFeedUseCase
    ) {
        this.createFeedUseCase = createFeedUseCase;
        this.updateFeedUseCase = updateFeedUseCase;
        this.deleteFeedUseCase = deleteFeedUseCase;
        this.addCommentUseCase = addCommentUseCase;
        this.updateCommentUseCase = updateCommentUseCase;
        this.addCommentHeartUseCase = addCommentHeartUseCase;
        this.removeCommentHeartUseCase = removeCommentHeartUseCase;
        this.addHeartUseCase = addHeartUseCase;
        this.removeHeartUseCase = removeHeartUseCase;
        this.reportFeedUseCase = reportFeedUseCase;
    }

    @PostMapping
    public ResponseEntity<FeedResponse> createFeed(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @RequestBody final CreateFeedRequest request
    ) {
        final Feed feed = createFeedUseCase.create(request.toCommand(signInSession.userId(), clubId));
        return ResponseEntity.ok(FeedResponse.from(feed));
    }

    @PostMapping("/{feedId}/update")
    public ResponseEntity<FeedResponse> updateFeed(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String feedId,
            @RequestBody final UpdateFeedRequest request
    ) {
        final Feed feed = updateFeedUseCase.update(request.toCommand(feedId, signInSession.userId(), clubId));
        return ResponseEntity.ok(FeedResponse.from(feed));
    }

    @PostMapping("/{feedId}/delete")
    public ResponseEntity<Void> deleteFeed(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String feedId
    ) {
        deleteFeedUseCase.delete(new DeleteFeedCommand(feedId, signInSession.userId(), clubId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{feedId}/comments")
    public ResponseEntity<FeedResponse> addComment(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String feedId,
            @RequestBody final AddCommentRequest request
    ) {
        final Feed feed = addCommentUseCase.addComment(request.toCommand(feedId, signInSession.userId(), clubId));
        return ResponseEntity.ok(FeedResponse.from(feed));
    }

    @PostMapping("/{feedId}/comments/{commentId}/update")
    public ResponseEntity<FeedResponse> updateComment(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String feedId,
            @PathVariable final String commentId,
            @RequestBody final UpdateCommentRequest request
    ) {
        final Feed feed = updateCommentUseCase.updateComment(request.toCommand(feedId, commentId, signInSession.userId(), clubId));
        return ResponseEntity.ok(FeedResponse.from(feed));
    }

    @PostMapping("/{feedId}/comments/{commentId}/hearts/add")
    public ResponseEntity<FeedResponse> addCommentHeart(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String feedId,
            @PathVariable final String commentId
    ) {
        final Feed feed = addCommentHeartUseCase.addCommentHeart(new AddCommentHeartCommand(feedId, commentId, signInSession.userId(), clubId));
        return ResponseEntity.ok(FeedResponse.from(feed));
    }

    @PostMapping("/{feedId}/comments/{commentId}/hearts/remove")
    public ResponseEntity<FeedResponse> removeCommentHeart(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String feedId,
            @PathVariable final String commentId
    ) {
        final Feed feed = removeCommentHeartUseCase.removeCommentHeart(new RemoveCommentHeartCommand(feedId, commentId, signInSession.userId(), clubId));
        return ResponseEntity.ok(FeedResponse.from(feed));
    }

    @PostMapping("/{feedId}/hearts/add")
    public ResponseEntity<FeedResponse> addHeart(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String feedId
    ) {
        final Feed feed = addHeartUseCase.addHeart(new AddHeartCommand(feedId, signInSession.userId(), clubId));
        return ResponseEntity.ok(FeedResponse.from(feed));
    }

    @PostMapping("/{feedId}/hearts/remove")
    public ResponseEntity<FeedResponse> removeHeart(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String feedId
    ) {
        final Feed feed = removeHeartUseCase.removeHeart(new RemoveHeartCommand(feedId, signInSession.userId(), clubId));
        return ResponseEntity.ok(FeedResponse.from(feed));
    }

    @PostMapping("/{feedId}/report")
    public ResponseEntity<Void> reportFeed(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String feedId,
            @RequestBody final ReportFeedRequest request
    ) {
        reportFeedUseCase.report(request.toCommand(feedId, signInSession.userId(), clubId));
        return ResponseEntity.ok().build();
    }
}
