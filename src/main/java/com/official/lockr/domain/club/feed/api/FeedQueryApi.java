package com.official.lockr.domain.club.feed.api;

import static org.jooq.generated.tables.FeedsJOOQEntity.FEEDS;
import static org.jooq.generated.tables.ClubsJOOQEntity.CLUBS;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;
import static org.jooq.generated.tables.UserAdditionalInfoJOOQEntity.USER_ADDITIONAL_INFO;
import static org.jooq.generated.tables.FeedHeartsJOOQEntity.FEED_HEARTS;
import static org.jooq.generated.tables.FeedImagesJOOQEntity.FEED_IMAGES;
import static org.jooq.generated.tables.FeedVideosJOOQEntity.FEED_VIDEOS;
import static org.jooq.generated.tables.CommentsJOOQEntity.COMMENTS;
import static org.jooq.generated.tables.CommentHeartsJOOQEntity.COMMENT_HEARTS;
import static org.jooq.generated.tables.CommentImagesJOOQEntity.COMMENT_IMAGES;
import static org.jooq.generated.tables.CommentVideosJOOQEntity.COMMENT_VIDEOS;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.feed.api.dto.CommentItemResponse;
import com.official.lockr.domain.club.feed.api.dto.CommentsResponse;
import com.official.lockr.domain.club.feed.api.dto.FeedItemResponse;
import com.official.lockr.domain.club.feed.api.dto.FeedsResponse;
import com.official.lockr.domain.club.feed.api.dto.HeartItemResponse;
import com.official.lockr.domain.club.feed.api.dto.HeartsResponse;
import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.generated.tables.daos.FeedsDao;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("/api/v1/clubs/{clubId}/feeds")
@RestController
public class FeedQueryApi {

    private static final Field<String> PARENT_COMMENT_ID = DSL.field(DSL.name("comments", "parent_comment_id"), SQLDataType.VARCHAR);

    private final FeedsDao feedsDao;

    public FeedQueryApi(final Configuration configuration) {
        this.feedsDao = new FeedsDao(configuration);
    }

    @GetMapping
    public ResponseEntity<FeedsResponse> getFeeds(
            @PathVariable String clubId,
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @RequestParam(value = "cursor", required = false, defaultValue = "") String cursor,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        // Check if user is a member of the club
        final boolean isMember = feedsDao.ctx()
                .fetchExists(
                        feedsDao.ctx()
                                .selectOne()
                                .from(MEMBERS)
                                .where(MEMBERS.CLUB_ID.eq(clubId))
                                .and(MEMBERS.USER_ID.eq(signInSession.userId()))
                                .and(MEMBERS.DELETED_AT.isNull())
                );

        if (!isMember) {
            return ResponseEntity.status(403).build();
        }

        // Step 1: Get feed IDs with pagination
        var feedIdsQuery = feedsDao.ctx()
                .select(FEEDS.ID)
                .from(FEEDS)
                .where(FEEDS.CLUB_ID.eq(clubId))
                .and(FEEDS.DELETED_AT.isNull());

        if (!cursor.isEmpty()) {
            feedIdsQuery = feedIdsQuery.and(FEEDS.ID.lt(cursor));
        }

        final var feedIds = feedIdsQuery
                .orderBy(FEEDS.CREATED_AT.desc())
                .limit(limit)
                .fetch()
                .map(record -> record.get(FEEDS.ID));

        if (feedIds.isEmpty()) {
            return ResponseEntity.ok(new FeedsResponse(List.of()));
        }

        // Step 2: Get images, videos, and likes (in-memory join)
        final Map<String, List<String>> feedImages = fetchFeedImages(feedIds);
        final Map<String, List<String>> feedVideos = fetchFeedVideos(feedIds);
        final Map<String, Boolean> feedLikes = fetchFeedLikes(feedIds, signInSession.userId());

        // Step 3: Fetch full feed data with joins and aggregations
        final List<FeedItemResponse> feeds = feedsDao.ctx()
                .select(
                        FEEDS.ID,
                        FEEDS.CLUB_ID,
                        CLUBS.NAME.as("club_name"),
                        FEEDS.USER_ID,
                        USER_ADDITIONAL_INFO.NAME.as("user_name"),
                        MEMBERS.MEMBER_ROLE,
                        FEEDS.TITLE,
                        FEEDS.CONTENT,
                        FEEDS.FEED_TYPE,
                        FEEDS.CREATED_AT,
                        FEEDS.UPDATED_AT,
                        DSL.countDistinct(FEED_HEARTS.ID).filterWhere(FEED_HEARTS.DELETED_AT.isNull()).as("hearts_count"),
                        DSL.countDistinct(COMMENTS.ID).filterWhere(COMMENTS.DELETED_AT.isNull()).as("comments_count")
                )
                .from(FEEDS)
                .innerJoin(CLUBS).on(CLUBS.ID.eq(FEEDS.CLUB_ID))
                .innerJoin(MEMBERS).on(
                        MEMBERS.USER_ID.eq(FEEDS.USER_ID)
                                .and(MEMBERS.CLUB_ID.eq(FEEDS.CLUB_ID))
                                .and(MEMBERS.DELETED_AT.isNull())
                )
                .leftJoin(USER_ADDITIONAL_INFO).on(USER_ADDITIONAL_INFO.USER_ID.eq(FEEDS.USER_ID))
                .leftJoin(FEED_HEARTS).on(FEED_HEARTS.FEED_ID.eq(FEEDS.ID))
                .leftJoin(COMMENTS).on(COMMENTS.FEED_ID.eq(FEEDS.ID))
                .where(FEEDS.ID.in(feedIds))
                .groupBy(
                        FEEDS.ID, FEEDS.CLUB_ID, CLUBS.NAME, FEEDS.USER_ID,
                        USER_ADDITIONAL_INFO.NAME, MEMBERS.MEMBER_ROLE,
                        FEEDS.CONTENT, FEEDS.FEED_TYPE, FEEDS.CREATED_AT, FEEDS.UPDATED_AT
                )
                .orderBy(FEEDS.CREATED_AT.desc())
                .fetch()
                .map(record -> new FeedItemResponse(
                        record.get(FEEDS.ID),
                        record.get(FEEDS.CLUB_ID),
                        record.get("club_name", String.class),
                        record.get(FEEDS.USER_ID),
                        record.get("user_name", String.class),
                        record.get(MEMBERS.MEMBER_ROLE),
                        record.get(FEEDS.TITLE),
                        record.get(FEEDS.CONTENT),
                        record.get(FEEDS.FEED_TYPE),
                        feedImages.getOrDefault(record.get(FEEDS.ID), List.of()),
                        feedVideos.getOrDefault(record.get(FEEDS.ID), List.of()),
                        record.get("hearts_count", Integer.class),
                        record.get("comments_count", Integer.class),
                        feedLikes.getOrDefault(record.get(FEEDS.ID), false),
                        record.get(FEEDS.CREATED_AT),
                        record.get(FEEDS.UPDATED_AT)
                ));

        return ResponseEntity.ok(new FeedsResponse(feeds));
    }

    private Map<String, List<String>> fetchFeedImages(List<String> feedIds) {
        return feedsDao.ctx()
                .select(FEED_IMAGES.FEED_ID, FEED_IMAGES.URL)
                .from(FEED_IMAGES)
                .where(FEED_IMAGES.FEED_ID.in(feedIds))
                .and(FEED_IMAGES.DELETED_AT.isNull())
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        record -> record.get(FEED_IMAGES.FEED_ID),
                        Collectors.mapping(record -> record.get(FEED_IMAGES.URL), Collectors.toList())
                ));
    }

    private Map<String, List<String>> fetchFeedVideos(List<String> feedIds) {
        return feedsDao.ctx()
                .select(FEED_VIDEOS.FEED_ID, FEED_VIDEOS.URL)
                .from(FEED_VIDEOS)
                .where(FEED_VIDEOS.FEED_ID.in(feedIds))
                .and(FEED_VIDEOS.DELETED_AT.isNull())
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        record -> record.get(FEED_VIDEOS.FEED_ID),
                        Collectors.mapping(record -> record.get(FEED_VIDEOS.URL), Collectors.toList())
                ));
    }

    private Map<String, Boolean> fetchFeedLikes(List<String> feedIds, String userId) {
        return feedsDao.ctx()
                .select(FEED_HEARTS.FEED_ID)
                .from(FEED_HEARTS)
                .where(FEED_HEARTS.FEED_ID.in(feedIds))
                .and(FEED_HEARTS.USER_ID.eq(userId))
                .and(FEED_HEARTS.DELETED_AT.isNull())
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        record -> record.get(FEED_HEARTS.FEED_ID),
                        _ -> true
                ));
    }

    @GetMapping("/{feedId}/comments")
    public ResponseEntity<CommentsResponse> getComments(
            @PathVariable String clubId,
            @PathVariable String feedId,
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @RequestParam(value = "cursor", required = false, defaultValue = "") String cursor,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        // Check if user is a member of the club
        final boolean isMember = feedsDao.ctx()
                .fetchExists(
                        feedsDao.ctx()
                                .selectOne()
                                .from(MEMBERS)
                                .where(MEMBERS.CLUB_ID.eq(clubId))
                                .and(MEMBERS.USER_ID.eq(signInSession.userId()))
                                .and(MEMBERS.DELETED_AT.isNull())
                );

        if (!isMember) {
            return ResponseEntity.status(403).build();
        }

        // Step 1: Get comment IDs with pagination
        var commentIdsQuery = feedsDao.ctx()
                .select(COMMENTS.ID)
                .from(COMMENTS)
                .where(COMMENTS.FEED_ID.eq(feedId))
                .and(COMMENTS.DELETED_AT.isNull());

        if (!cursor.isEmpty()) {
            commentIdsQuery = commentIdsQuery.and(COMMENTS.ID.lt(cursor));
        }

        final var commentIds = commentIdsQuery
                .orderBy(COMMENTS.CREATED_AT.desc())
                .limit(limit)
                .fetch()
                .map(record -> record.get(COMMENTS.ID));

        if (commentIds.isEmpty()) {
            return ResponseEntity.ok(new CommentsResponse(List.of()));
        }

        // Step 2: Get images, videos, and likes (in-memory join)
        final Map<String, List<String>> commentImages = fetchCommentImages(commentIds);
        final Map<String, List<String>> commentVideos = fetchCommentVideos(commentIds);
        final Map<String, Boolean> commentLikes = fetchCommentLikes(commentIds, signInSession.userId());

        // Step 3: Fetch full comment data with user info and aggregations
        final List<CommentItemResponse> comments = feedsDao.ctx()
                .select(
                        COMMENTS.ID,
                        COMMENTS.FEED_ID,
                        COMMENTS.USER_ID,
                        USER_ADDITIONAL_INFO.NAME.as("user_name"),
                        COMMENTS.CONTENT,
                        PARENT_COMMENT_ID,
                        COMMENTS.CREATED_AT,
                        COMMENTS.UPDATED_AT,
                        DSL.countDistinct(COMMENT_HEARTS.ID).filterWhere(COMMENT_HEARTS.DELETED_AT.isNull()).as("hearts_count")
                )
                .from(COMMENTS)
                .leftJoin(USER_ADDITIONAL_INFO).on(USER_ADDITIONAL_INFO.USER_ID.eq(COMMENTS.USER_ID))
                .leftJoin(COMMENT_HEARTS).on(COMMENT_HEARTS.COMMENT_ID.eq(COMMENTS.ID))
                .where(COMMENTS.ID.in(commentIds))
                .groupBy(
                        COMMENTS.ID, COMMENTS.FEED_ID, COMMENTS.USER_ID,
                        USER_ADDITIONAL_INFO.NAME, COMMENTS.CONTENT,
                        PARENT_COMMENT_ID,
                        COMMENTS.CREATED_AT, COMMENTS.UPDATED_AT
                )
                .orderBy(COMMENTS.CREATED_AT.desc())
                .fetch()
                .map(record -> new CommentItemResponse(
                        record.get(COMMENTS.ID),
                        record.get(COMMENTS.FEED_ID),
                        record.get(COMMENTS.USER_ID),
                        record.get("user_name", String.class),
                        record.get(COMMENTS.CONTENT),
                        record.get(PARENT_COMMENT_ID),
                        commentImages.getOrDefault(record.get(COMMENTS.ID), List.of()),
                        commentVideos.getOrDefault(record.get(COMMENTS.ID), List.of()),
                        record.get("hearts_count", Integer.class),
                        commentLikes.getOrDefault(record.get(COMMENTS.ID), false),
                        record.get(COMMENTS.CREATED_AT),
                        record.get(COMMENTS.UPDATED_AT)
                ));

        return ResponseEntity.ok(new CommentsResponse(comments));
    }

    private Map<String, List<String>> fetchCommentImages(List<String> commentIds) {
        return feedsDao.ctx()
                .select(COMMENT_IMAGES.COMMENT_ID, COMMENT_IMAGES.URL)
                .from(COMMENT_IMAGES)
                .where(COMMENT_IMAGES.COMMENT_ID.in(commentIds))
                .and(COMMENT_IMAGES.DELETED_AT.isNull())
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        record -> record.get(COMMENT_IMAGES.COMMENT_ID),
                        Collectors.mapping(record -> record.get(COMMENT_IMAGES.URL), Collectors.toList())
                ));
    }

    private Map<String, List<String>> fetchCommentVideos(List<String> commentIds) {
        return feedsDao.ctx()
                .select(COMMENT_VIDEOS.COMMENT_ID, COMMENT_VIDEOS.URL)
                .from(COMMENT_VIDEOS)
                .where(COMMENT_VIDEOS.COMMENT_ID.in(commentIds))
                .and(COMMENT_VIDEOS.DELETED_AT.isNull())
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        record -> record.get(COMMENT_VIDEOS.COMMENT_ID),
                        Collectors.mapping(record -> record.get(COMMENT_VIDEOS.URL), Collectors.toList())
                ));
    }

    private Map<String, Boolean> fetchCommentLikes(List<String> commentIds, String userId) {
        return feedsDao.ctx()
                .select(COMMENT_HEARTS.COMMENT_ID)
                .from(COMMENT_HEARTS)
                .where(COMMENT_HEARTS.COMMENT_ID.in(commentIds))
                .and(COMMENT_HEARTS.USER_ID.eq(userId))
                .and(COMMENT_HEARTS.DELETED_AT.isNull())
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        record -> record.get(COMMENT_HEARTS.COMMENT_ID),
                        record -> true
                ));
    }

    @GetMapping("/{feedId}/comments/{commentId}/hearts")
    public ResponseEntity<HeartsResponse> getCommentHearts(
            @PathVariable String clubId,
            @PathVariable String feedId,
            @PathVariable String commentId,
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @RequestParam(value = "cursor", required = false, defaultValue = "") String cursor,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        final boolean isMember = feedsDao.ctx()
                .fetchExists(
                        feedsDao.ctx()
                                .selectOne()
                                .from(MEMBERS)
                                .where(MEMBERS.CLUB_ID.eq(clubId))
                                .and(MEMBERS.USER_ID.eq(signInSession.userId()))
                                .and(MEMBERS.DELETED_AT.isNull())
                );

        if (!isMember) {
            return ResponseEntity.status(403).build();
        }

        var heartsQuery = feedsDao.ctx()
                .select(
                        COMMENT_HEARTS.ID,
                        COMMENT_HEARTS.COMMENT_ID,
                        COMMENT_HEARTS.USER_ID,
                        USER_ADDITIONAL_INFO.NAME.as("user_name"),
                        COMMENT_HEARTS.CREATED_AT
                )
                .from(COMMENT_HEARTS)
                .leftJoin(USER_ADDITIONAL_INFO).on(USER_ADDITIONAL_INFO.USER_ID.eq(COMMENT_HEARTS.USER_ID))
                .where(COMMENT_HEARTS.COMMENT_ID.eq(commentId))
                .and(COMMENT_HEARTS.DELETED_AT.isNull());

        if (!cursor.isEmpty()) {
            heartsQuery = heartsQuery.and(COMMENT_HEARTS.ID.lt(cursor));
        }

        final List<HeartItemResponse> hearts = heartsQuery
                .orderBy(COMMENT_HEARTS.CREATED_AT.desc())
                .limit(limit)
                .fetch()
                .map(record -> new HeartItemResponse(
                        record.get(COMMENT_HEARTS.ID),
                        record.get(COMMENT_HEARTS.COMMENT_ID),
                        record.get(COMMENT_HEARTS.USER_ID),
                        record.get("user_name", String.class),
                        record.get(COMMENT_HEARTS.CREATED_AT)
                ));

        return ResponseEntity.ok(new HeartsResponse(hearts));
    }

    @GetMapping("/{feedId}/hearts")
    public ResponseEntity<HeartsResponse> getHearts(
            @PathVariable String clubId,
            @PathVariable String feedId,
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @RequestParam(value = "cursor", required = false, defaultValue = "") String cursor,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        // Check if user is a member of the club
        final boolean isMember = feedsDao.ctx()
                .fetchExists(
                        feedsDao.ctx()
                                .selectOne()
                                .from(MEMBERS)
                                .where(MEMBERS.CLUB_ID.eq(clubId))
                                .and(MEMBERS.USER_ID.eq(signInSession.userId()))
                                .and(MEMBERS.DELETED_AT.isNull())
                );

        if (!isMember) {
            return ResponseEntity.status(403).build();
        }

        // Get hearts with pagination
        var heartsQuery = feedsDao.ctx()
                .select(
                        FEED_HEARTS.ID,
                        FEED_HEARTS.FEED_ID,
                        FEED_HEARTS.USER_ID,
                        USER_ADDITIONAL_INFO.NAME.as("user_name"),
                        FEED_HEARTS.CREATED_AT
                )
                .from(FEED_HEARTS)
                .leftJoin(USER_ADDITIONAL_INFO).on(USER_ADDITIONAL_INFO.USER_ID.eq(FEED_HEARTS.USER_ID))
                .where(FEED_HEARTS.FEED_ID.eq(feedId))
                .and(FEED_HEARTS.DELETED_AT.isNull());

        if (!cursor.isEmpty()) {
            heartsQuery = heartsQuery.and(FEED_HEARTS.ID.lt(cursor));
        }

        final List<HeartItemResponse> hearts = heartsQuery
                .orderBy(FEED_HEARTS.CREATED_AT.desc())
                .limit(limit)
                .fetch()
                .map(record -> new HeartItemResponse(
                        record.get(FEED_HEARTS.ID),
                        record.get(FEED_HEARTS.FEED_ID),
                        record.get(FEED_HEARTS.USER_ID),
                        record.get("user_name", String.class),
                        record.get(FEED_HEARTS.CREATED_AT)
                ));

        return ResponseEntity.ok(new HeartsResponse(hearts));
    }

    @GetMapping("/{feedId}")
    public ResponseEntity<FeedItemResponse> getFeed(
            @PathVariable String clubId,
            @PathVariable String feedId,
            @RequestAttribute("signInSession") final SignInSession signInSession
    ) {
        // Check if user is a member of the club
        final boolean isMember = feedsDao.ctx()
                .fetchExists(
                        feedsDao.ctx()
                                .selectOne()
                                .from(MEMBERS)
                                .where(MEMBERS.CLUB_ID.eq(clubId))
                                .and(MEMBERS.USER_ID.eq(signInSession.userId()))
                                .and(MEMBERS.DELETED_AT.isNull())
                );

        if (!isMember) {
            return ResponseEntity.status(403).build();
        }

        // Get images, videos, and likes
        final Map<String, List<String>> feedImages = fetchFeedImages(List.of(feedId));
        final Map<String, List<String>> feedVideos = fetchFeedVideos(List.of(feedId));
        final Map<String, Boolean> feedLikes = fetchFeedLikes(List.of(feedId), signInSession.userId());

        // Fetch feed data
        final FeedItemResponse feed = feedsDao.ctx()
                .select(
                        FEEDS.ID,
                        FEEDS.CLUB_ID,
                        CLUBS.NAME.as("club_name"),
                        FEEDS.USER_ID,
                        USER_ADDITIONAL_INFO.NAME.as("user_name"),
                        MEMBERS.MEMBER_ROLE,
                        FEEDS.CONTENT,
                        FEEDS.FEED_TYPE,
                        FEEDS.CREATED_AT,
                        FEEDS.UPDATED_AT,
                        DSL.countDistinct(FEED_HEARTS.ID).filterWhere(FEED_HEARTS.DELETED_AT.isNull()).as("hearts_count"),
                        DSL.countDistinct(COMMENTS.ID).filterWhere(COMMENTS.DELETED_AT.isNull()).as("comments_count")
                )
                .from(FEEDS)
                .innerJoin(CLUBS).on(CLUBS.ID.eq(FEEDS.CLUB_ID))
                .innerJoin(MEMBERS).on(
                        MEMBERS.USER_ID.eq(FEEDS.USER_ID)
                                .and(MEMBERS.CLUB_ID.eq(FEEDS.CLUB_ID))
                                .and(MEMBERS.DELETED_AT.isNull())
                )
                .leftJoin(USER_ADDITIONAL_INFO).on(USER_ADDITIONAL_INFO.USER_ID.eq(FEEDS.USER_ID))
                .leftJoin(FEED_HEARTS).on(FEED_HEARTS.FEED_ID.eq(FEEDS.ID))
                .leftJoin(COMMENTS).on(COMMENTS.FEED_ID.eq(FEEDS.ID))
                .where(FEEDS.ID.eq(feedId))
                .and(FEEDS.CLUB_ID.eq(clubId))
                .and(FEEDS.DELETED_AT.isNull())
                .groupBy(
                        FEEDS.ID, FEEDS.CLUB_ID, CLUBS.NAME, FEEDS.USER_ID,
                        USER_ADDITIONAL_INFO.NAME, MEMBERS.MEMBER_ROLE,
                        FEEDS.CONTENT, FEEDS.FEED_TYPE, FEEDS.CREATED_AT, FEEDS.UPDATED_AT
                )
                .fetchOne(record -> new FeedItemResponse(
                        record.get(FEEDS.ID),
                        record.get(FEEDS.CLUB_ID),
                        record.get("club_name", String.class),
                        record.get(FEEDS.USER_ID),
                        record.get("user_name", String.class),
                        record.get(MEMBERS.MEMBER_ROLE),
                        record.get(FEEDS.TITLE),
                        record.get(FEEDS.CONTENT),
                        record.get(FEEDS.FEED_TYPE),
                        feedImages.getOrDefault(feedId, List.of()),
                        feedVideos.getOrDefault(feedId, List.of()),
                        record.get("hearts_count", Integer.class),
                        record.get("comments_count", Integer.class),
                        feedLikes.getOrDefault(feedId, false),
                        record.get(FEEDS.CREATED_AT),
                        record.get(FEEDS.UPDATED_AT)
                ));

        if (feed == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(feed);
    }
}
