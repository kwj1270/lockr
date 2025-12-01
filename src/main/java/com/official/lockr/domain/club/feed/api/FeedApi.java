package com.official.lockr.domain.club.feed.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.feed.api.dto.*;
import com.official.lockr.domain.club.feed.application.dto.*;
import com.official.lockr.domain.club.feed.application.usecase.*;
import com.official.lockr.domain.club.feed.domain.Feed;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static java.util.Objects.isNull;

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

    public FeedApi(
            final CreateFeedUseCase createFeedUseCase,
            final UpdateFeedUseCase updateFeedUseCase,
            final DeleteFeedUseCase deleteFeedUseCase,
            final AddCommentUseCase addCommentUseCase,
            final UpdateCommentUseCase updateCommentUseCase,
            final AddCommentHeartUseCase addCommentHeartUseCase,
            final RemoveCommentHeartUseCase removeCommentHeartUseCase,
            final AddHeartUseCase addHeartUseCase,
            final RemoveHeartUseCase removeHeartUseCase
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
    }

    @PostMapping
    public ResponseEntity<FeedResponse> createFeed(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @RequestBody final CreateFeedRequest request
    ) {
        final SignInSession session = session(httpSession);

        final Feed feed = createFeedUseCase.create(new CreateFeedCommand(
                session.userId(),
                clubId,
                request.content(),
                request.feedType(),
                request.imageUrls(),
                request.videoUrls()
        ));

        return ResponseEntity.ok(FeedResponse.from(feed));
    }

    @PostMapping("/{feedId}/update")
    public ResponseEntity<FeedResponse> updateFeed(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String feedId,
            @RequestBody final UpdateFeedRequest request
    ) {
        final SignInSession session = session(httpSession);

        final Feed feed = updateFeedUseCase.update(new UpdateFeedCommand(
                feedId,
                session.userId(),
                clubId,
                request.content(),
                request.imageUrls(),
                request.videoUrls()
        ));

        return ResponseEntity.ok(FeedResponse.from(feed));
    }

    @PostMapping("/{feedId}/delete")
    public ResponseEntity<Void> deleteFeed(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String feedId
    ) {
        final SignInSession session = session(httpSession);

        deleteFeedUseCase.delete(new DeleteFeedCommand(
                feedId,
                session.userId(),
                clubId
        ));

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{feedId}/comments")
    public ResponseEntity<FeedResponse> addComment(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String feedId,
            @RequestBody final AddCommentRequest request
    ) {
        final SignInSession session = session(httpSession);

        final Feed feed = addCommentUseCase.addComment(new AddCommentCommand(
                feedId,
                session.userId(),
                clubId,
                request.content(),
                request.imageUrls(),
                request.videoUrls()
        ));

        return ResponseEntity.ok(FeedResponse.from(feed));
    }

    @PostMapping("/{feedId}/comments/{commentId}/update")
    public ResponseEntity<FeedResponse> updateComment(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String feedId,
            @PathVariable final String commentId,
            @RequestBody final UpdateCommentRequest request
    ) {
        final SignInSession session = session(httpSession);

        final Feed feed = updateCommentUseCase.updateComment(new UpdateCommentCommand(
                feedId,
                commentId,
                session.userId(),
                clubId,
                request.content(),
                request.imageUrls(),
                request.videoUrls()
        ));

        return ResponseEntity.ok(FeedResponse.from(feed));
    }

    @PostMapping("/{feedId}/comments/{commentId}/hearts/add")
    public ResponseEntity<FeedResponse> addCommentHeart(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String feedId,
            @PathVariable final String commentId
    ) {
        final SignInSession session = session(httpSession);

        final Feed feed = addCommentHeartUseCase.addCommentHeart(new AddCommentHeartCommand(
                feedId,
                commentId,
                session.userId(),
                clubId
        ));

        return ResponseEntity.ok(FeedResponse.from(feed));
    }

    @PostMapping("/{feedId}/comments/{commentId}/hearts/remove")
    public ResponseEntity<FeedResponse> removeCommentHeart(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String feedId,
            @PathVariable final String commentId
    ) {
        final SignInSession session = session(httpSession);

        final Feed feed = removeCommentHeartUseCase.removeCommentHeart(new RemoveCommentHeartCommand(
                feedId,
                commentId,
                session.userId(),
                clubId
        ));

        return ResponseEntity.ok(FeedResponse.from(feed));
    }

    @PostMapping("/{feedId}/hearts/add")
    public ResponseEntity<FeedResponse> addHeart(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String feedId
    ) {
        final SignInSession session = session(httpSession);

        final Feed feed = addHeartUseCase.addHeart(new AddHeartCommand(feedId, session.userId(), clubId));

        return ResponseEntity.ok(FeedResponse.from(feed));
    }

    @PostMapping("/{feedId}/hearts/remove")
    public ResponseEntity<FeedResponse> removeHeart(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String feedId
    ) {
        final SignInSession session = session(httpSession);

        final Feed feed = removeHeartUseCase.removeHeart(new RemoveHeartCommand(
                feedId,
                session.userId(),
                clubId
        ));

        return ResponseEntity.ok(FeedResponse.from(feed));
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession session = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(session)) {
            throw new IllegalArgumentException("Not signed in");
        }
        return session;
    }
}
