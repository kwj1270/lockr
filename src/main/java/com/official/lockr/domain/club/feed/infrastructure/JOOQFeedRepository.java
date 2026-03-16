package com.official.lockr.domain.club.feed.infrastructure;

import com.official.lockr.domain.club.feed.domain.*;
import com.official.lockr.domain.club.feed.domain.comment.*;
import com.official.lockr.domain.club.feed.domain.entity.Heart;
import com.official.lockr.domain.club.feed.domain.entity.Image;
import com.official.lockr.domain.club.feed.domain.entity.Video;
import com.official.lockr.global.ddd.DomainEventPublisher;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.JSON;
import org.jooq.generated.tables.daos.*;
import org.jooq.generated.tables.pojos.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.generated.Tables.FEED_HEARTS;
import static org.jooq.generated.tables.FeedsJOOQEntity.FEEDS;
import static org.jooq.generated.tables.FeedImagesJOOQEntity.FEED_IMAGES;
import static org.jooq.generated.tables.FeedVideosJOOQEntity.FEED_VIDEOS;
import static org.jooq.generated.tables.CommentsJOOQEntity.COMMENTS;
import static org.jooq.generated.tables.CommentImagesJOOQEntity.COMMENT_IMAGES;
import static org.jooq.generated.tables.CommentVideosJOOQEntity.COMMENT_VIDEOS;
import static org.jooq.generated.tables.CommentHeartsJOOQEntity.COMMENT_HEARTS;
import static org.jooq.impl.DSL.excluded;

@Repository
public class JOOQFeedRepository implements FeedRepository {

    private static final Field<JSON> METADATA = DSL.field(DSL.name("metadata"), SQLDataType.JSON);
    private static final Field<String> PARENT_COMMENT_ID = DSL.field(DSL.name("parent_comment_id"), SQLDataType.VARCHAR);

    private final FeedsDao feedsDao;
    private final FeedImagesDao feedImagesDao;
    private final FeedVideosDao feedVideosDao;
    private final CommentsDao commentsDao;
    private final CommentImagesDao commentImagesDao;
    private final CommentVideosDao commentVideosDao;
    private final FeedHeartsDao feedHeartsDao;
    private final CommentHeartsDao commentHeartsDao;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQFeedRepository(
            final Configuration configuration,
            final DomainEventPublisher domainEventPublisher
    ) {
        this.feedsDao = new FeedsDao(configuration);
        this.feedImagesDao = new FeedImagesDao(configuration);
        this.feedVideosDao = new FeedVideosDao(configuration);
        this.commentsDao = new CommentsDao(configuration);
        this.commentImagesDao = new CommentImagesDao(configuration);
        this.commentVideosDao = new CommentVideosDao(configuration);
        this.feedHeartsDao = new FeedHeartsDao(configuration);
        this.commentHeartsDao = new CommentHeartsDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Nullable
    @Override
    public Feed findById(final String id) {
        final FeedsEntity entity = feedsDao.ctx()
                .selectFrom(FEEDS)
                .where(FEEDS.ID.eq(id))
                .and(FEEDS.DELETED_AT.isNull())
                .fetchOneInto(FeedsEntity.class);

        if (entity == null) {
            return null;
        }

        return toDomain(entity);
    }

    @Nullable
    @Override
    public Feed findByScheduleId(final String scheduleId) {
        final var record = feedsDao.ctx()
                .select(FEEDS.fields())
                .select(METADATA)
                .from(FEEDS)
                .where(DSL.condition("JSON_UNQUOTE(metadata->'$.scheduleId') = ?", scheduleId))
                .and(FEEDS.DELETED_AT.isNull())
                .fetchOne();

        if (record == null) {
            return null;
        }

        final FeedsEntity entity = record.into(FeedsEntity.class);
        final JSON metadataJson = record.get(METADATA);
        final String metadata = metadataJson != null ? metadataJson.data() : null;

        return toDomain(entity, metadata);
    }

    @Override
    public List<Feed> findAllByClubId(final String clubId) {
        final List<FeedsEntity> entities = feedsDao.ctx()
                .selectFrom(FEEDS)
                .where(FEEDS.CLUB_ID.eq(clubId))
                .and(FEEDS.DELETED_AT.isNull())
                .orderBy(FEEDS.CREATED_AT.desc())
                .fetchInto(FeedsEntity.class);

        if (entities.isEmpty()) {
            return Collections.emptyList();
        }

        final List<String> feedIds = entities.stream()
                .map(FeedsEntity::getId)
                .toList();

        // Batch fetch all related data
        final Map<String, List<FeedImagesEntity>> feedImagesMap = fetchFeedImagesMap(feedIds);
        final Map<String, List<FeedVideosEntity>> feedVideosMap = fetchFeedVideosMap(feedIds);
        final Map<String, List<FeedHeartsEntity>> feedHeartsMap = fetchFeedHeartsMap(feedIds);
        final Map<String, List<CommentRecord>> feedCommentsMap = fetchFeedCommentsMap(feedIds);

        // Batch fetch comment-related data
        final List<String> commentIds = feedCommentsMap.values().stream()
                .flatMap(List::stream)
                .map(record -> record.entity.getId())
                .toList();

        final Map<String, List<CommentImagesEntity>> commentImagesMap = fetchCommentImagesMap(commentIds);
        final Map<String, List<CommentVideosEntity>> commentVideosMap = fetchCommentVideosMap(commentIds);
        final Map<String, List<CommentHeartsEntity>> commentHeartsMap = fetchCommentHeartsMap(commentIds);

        return entities.stream()
                .map(entity -> toDomainWithBatchedData(
                        entity,
                        null,
                        feedImagesMap,
                        feedVideosMap,
                        feedHeartsMap,
                        feedCommentsMap,
                        commentImagesMap,
                        commentVideosMap,
                        commentHeartsMap
                ))
                .toList();
    }

    @Transactional
    @Override
    public Feed save(final Feed feed) {
        upsertFeed(feed);
        syncFeedImages(feed);
        syncFeedVideos(feed);
        syncComments(feed);
        syncHearts(feed);
        feed.publish(domainEventPublisher);
        return feed;
    }

    private void upsertFeed(final Feed feed) {
        feedsDao.ctx()
                .insertInto(FEEDS)
                .set(FEEDS.ID, feed.getId())
                .set(FEEDS.CLUB_ID, feed.getClubId())
                .set(FEEDS.USER_ID, feed.getUserId())
                .set(FEEDS.TITLE, feed.getTitle())
                .set(FEEDS.CONTENT, feed.getContent())
                .set(FEEDS.FEED_TYPE, feed.getFeedType().name())
                .set(METADATA, feed.getMetadata() != null ? JSON.json(feed.getMetadata()) : null)
                .set(FEEDS.CREATED_AT, feed.getCreatedAt())
                .set(FEEDS.UPDATED_AT, feed.getUpdatedAt())
                .set(FEEDS.DELETED_AT, feed.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(FEEDS.TITLE, excluded(FEEDS.TITLE))
                .set(FEEDS.CONTENT, excluded(FEEDS.CONTENT))
                .set(FEEDS.FEED_TYPE, excluded(FEEDS.FEED_TYPE))
                .set(FEEDS.UPDATED_AT, excluded(FEEDS.UPDATED_AT))
                .set(FEEDS.DELETED_AT, excluded(FEEDS.DELETED_AT))
                .execute();
    }

    private void syncFeedImages(final Feed feed) {
        final String feedId = feed.getId();
        final List<Image> newImages = feed.getImages().getImages();

        for (final Image image : newImages) {
            feedImagesDao.ctx()
                    .insertInto(FEED_IMAGES)
                    .set(FEED_IMAGES.ID, image.getId())
                    .set(FEED_IMAGES.FEED_ID, feedId)
                    .set(FEED_IMAGES.USER_ID, image.getUserId())
                    .set(FEED_IMAGES.URL, image.getUrl())
                    .set(FEED_IMAGES.CREATED_AT, image.getCreatedAt())
                    .set(FEED_IMAGES.DELETED_AT, image.getDeletedAt())
                    .onDuplicateKeyUpdate()
                    .set(FEED_IMAGES.DELETED_AT, excluded(FEED_IMAGES.DELETED_AT))
                    .execute();
        }
    }

    private void syncFeedVideos(final Feed feed) {
        final String feedId = feed.getId();
        final List<Video> newVideos = feed.getVideos().getVideos();

        for (final Video video : newVideos) {
            feedVideosDao.ctx()
                    .insertInto(FEED_VIDEOS)
                    .set(FEED_VIDEOS.ID, video.getId())
                    .set(FEED_VIDEOS.FEED_ID, feedId)
                    .set(FEED_VIDEOS.USER_ID, video.getUserId())
                    .set(FEED_VIDEOS.URL, video.getUrl())
                    .set(FEED_VIDEOS.CREATED_AT, video.getCreatedAt())
                    .set(FEED_VIDEOS.DELETED_AT, video.getDeletedAt())
                    .onDuplicateKeyUpdate()
                    .set(FEED_VIDEOS.DELETED_AT, excluded(FEED_VIDEOS.DELETED_AT))
                    .execute();
        }
    }

    private void syncComments(final Feed feed) {
        final String feedId = feed.getId();

        final List<Comment> newComments = feed.getComments();

        for (final Comment comment : newComments) {
            // Comment 자체 upsert
            commentsDao.ctx()
                    .insertInto(COMMENTS)
                    .set(COMMENTS.ID, comment.getId())
                    .set(COMMENTS.FEED_ID, feedId)
                    .set(COMMENTS.USER_ID, comment.getUserId())
                    .set(PARENT_COMMENT_ID, comment.getParentCommentId())
                    .set(COMMENTS.CONTENT, comment.getContent())
                    .set(COMMENTS.CREATED_AT, comment.getCreatedAt())
                    .set(COMMENTS.UPDATED_AT, comment.getUpdatedAt())
                    .set(COMMENTS.DELETED_AT, comment.getDeletedAt())
                    .onDuplicateKeyUpdate()
                    .set(COMMENTS.CONTENT, excluded(COMMENTS.CONTENT))
                    .set(COMMENTS.UPDATED_AT, excluded(COMMENTS.UPDATED_AT))
                    .set(COMMENTS.DELETED_AT, excluded(COMMENTS.DELETED_AT))
                    .execute();

            // Comment Images sync
            syncCommentImages(comment);
            // Comment Videos sync
            syncCommentVideos(comment);
            // Comment Hearts sync
            syncCommentHearts(comment);
        }
    }

    private void syncCommentImages(final Comment comment) {
        final String commentId = comment.getId();
        final List<Image> images = comment.getImages().getImages();

        for (final Image image : images) {
            commentImagesDao.ctx()
                    .insertInto(COMMENT_IMAGES)
                    .set(COMMENT_IMAGES.ID, image.getId())
                    .set(COMMENT_IMAGES.COMMENT_ID, commentId)
                    .set(COMMENT_IMAGES.USER_ID, image.getUserId())
                    .set(COMMENT_IMAGES.URL, image.getUrl())
                    .set(COMMENT_IMAGES.CREATED_AT, image.getCreatedAt())
                    .set(COMMENT_IMAGES.DELETED_AT, image.getDeletedAt())
                    .onDuplicateKeyUpdate()
                    .set(COMMENT_IMAGES.DELETED_AT, excluded(COMMENT_IMAGES.DELETED_AT))
                    .execute();
        }
    }

    private void syncCommentVideos(final Comment comment) {
        final String commentId = comment.getId();
        final List<Video> videos = comment.getVideos().getVideos();

        for (final Video video : videos) {
            commentVideosDao.ctx()
                    .insertInto(COMMENT_VIDEOS)
                    .set(COMMENT_VIDEOS.ID, video.getId())
                    .set(COMMENT_VIDEOS.COMMENT_ID, commentId)
                    .set(COMMENT_VIDEOS.USER_ID, video.getUserId())
                    .set(COMMENT_VIDEOS.URL, video.getUrl())
                    .set(COMMENT_VIDEOS.CREATED_AT, video.getCreatedAt())
                    .set(COMMENT_VIDEOS.DELETED_AT, video.getDeletedAt())
                    .onDuplicateKeyUpdate()
                    .set(COMMENT_VIDEOS.DELETED_AT, excluded(COMMENT_VIDEOS.DELETED_AT))
                    .execute();
        }
    }

    private void syncCommentHearts(final Comment comment) {
        final String commentId = comment.getId();
        final List<CommentHeart> hearts = comment.getHearts();

        for (final CommentHeart heart : hearts) {
            commentHeartsDao.ctx()
                    .insertInto(COMMENT_HEARTS)
                    .set(COMMENT_HEARTS.ID, heart.getId())
                    .set(COMMENT_HEARTS.COMMENT_ID, commentId)
                    .set(COMMENT_HEARTS.USER_ID, heart.getUserId())
                    .set(COMMENT_HEARTS.CREATED_AT, heart.getCreatedAt())
                    .set(COMMENT_HEARTS.DELETED_AT, heart.getDeletedAt())
                    .onDuplicateKeyUpdate()
                    .set(COMMENT_HEARTS.DELETED_AT, excluded(COMMENT_HEARTS.DELETED_AT))
                    .execute();
        }
    }

    private void syncHearts(final Feed feed) {
        final String feedId = feed.getId();

        final List<Heart> newHearts = feed.getHearts();

        for (final Heart heart : newHearts) {
            feedHeartsDao.ctx()
                    .insertInto(FEED_HEARTS)
                    .set(FEED_HEARTS.ID, heart.getId())
                    .set(FEED_HEARTS.FEED_ID, feedId)
                    .set(FEED_HEARTS.USER_ID, heart.getUserId())
                    .set(FEED_HEARTS.CREATED_AT, heart.getCreatedAt())
                    .set(FEED_HEARTS.DELETED_AT, heart.getDeletedAt())
                    .onDuplicateKeyUpdate()
                    .set(FEED_HEARTS.DELETED_AT, excluded(FEED_HEARTS.DELETED_AT))
                    .execute();
        }
    }

    private Feed toDomain(final FeedsEntity entity) {
        return toDomain(entity, null);
    }

    private Feed toDomain(final FeedsEntity entity, final String metadata) {
        final String feedId = entity.getId();

        // Feed Images 조회
        final List<FeedImagesEntity> imageEntities = feedImagesDao.ctx()
                .selectFrom(FEED_IMAGES)
                .where(FEED_IMAGES.FEED_ID.eq(feedId))
                .fetchInto(FeedImagesEntity.class);

        final List<Image> images = imageEntities.stream()
                .map(img -> new Image(img.getId(), img.getUrl(), img.getUserId(), img.getCreatedAt(), img.getDeletedAt()))
                .collect(Collectors.toCollection(ArrayList::new));

        final FeedImages feedImages = new FeedImages(feedId, images);

        // Feed Videos 조회
        final List<FeedVideosEntity> videoEntities = feedVideosDao.ctx()
                .selectFrom(FEED_VIDEOS)
                .where(FEED_VIDEOS.FEED_ID.eq(feedId))
                .fetchInto(FeedVideosEntity.class);

        final List<Video> videos = videoEntities.stream()
                .map(vid -> new Video(vid.getId(), vid.getUrl(), vid.getUserId(), vid.getCreatedAt(), vid.getDeletedAt()))
                .collect(Collectors.toCollection(ArrayList::new));
        final FeedVideos feedVideos = new FeedVideos(feedId, videos);

        // Comments 조회
        final var commentRecords = commentsDao.ctx()
                .select(COMMENTS.fields())
                .select(PARENT_COMMENT_ID)
                .from(COMMENTS)
                .where(COMMENTS.FEED_ID.eq(feedId))
                .fetch();

        final List<Comment> comments = commentRecords.stream()
                .map(record -> {
                    final CommentsEntity ce = record.into(CommentsEntity.class);
                    final String parentId = record.get(PARENT_COMMENT_ID);
                    return commentToDomain(ce, parentId);
                })
                .collect(Collectors.toCollection(ArrayList::new));

        // Hearts 조회
        final List<FeedHeartsEntity> heartEntities = feedHeartsDao.ctx()
                .selectFrom(FEED_HEARTS)
                .where(FEED_HEARTS.FEED_ID.eq(feedId))
                .fetchInto(FeedHeartsEntity.class);

        final List<Heart> hearts = heartEntities.stream()
                .map(h -> new Heart(h.getId(), h.getFeedId(), h.getUserId(), h.getCreatedAt(), h.getDeletedAt()))
                .collect(Collectors.toCollection(ArrayList::new));

        return new Feed(
                entity.getId(),
                entity.getClubId(),
                entity.getTitle(),
                entity.getContent(),
                FeedType.valueOf(entity.getFeedType()),
                metadata,
                feedImages,
                feedVideos,
                comments,
                hearts,
                entity.getUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private Comment commentToDomain(final CommentsEntity entity, final String parentCommentId) {
        final String commentId = entity.getId();

        // Comment Images
        final List<CommentImagesEntity> imageEntities = commentImagesDao.ctx()
                .selectFrom(COMMENT_IMAGES)
                .where(COMMENT_IMAGES.COMMENT_ID.eq(commentId))
                .fetchInto(CommentImagesEntity.class);

        final List<Image> images = imageEntities.stream()
                .map(img -> new Image(img.getId(), img.getUrl(), img.getUserId(), img.getCreatedAt(), img.getDeletedAt()))
                .collect(Collectors.toCollection(ArrayList::new));
        final CommentImages commentImages = new CommentImages(commentId, images);

        // Comment Videos
        final List<CommentVideosEntity> videoEntities = commentVideosDao.ctx()
                .selectFrom(COMMENT_VIDEOS)
                .where(COMMENT_VIDEOS.COMMENT_ID.eq(commentId))
                .fetchInto(CommentVideosEntity.class);

        final List<Video> videos = videoEntities.stream()
                .map(vid -> new Video(vid.getId(), vid.getUrl(), vid.getUserId(), vid.getCreatedAt(), vid.getDeletedAt()))
                .collect(Collectors.toCollection(ArrayList::new));
        final CommentVideos commentVideos = new CommentVideos(commentId, videos);

        // Comment Hearts
        final List<CommentHeartsEntity> heartEntities = commentHeartsDao.ctx()
                .selectFrom(COMMENT_HEARTS)
                .where(COMMENT_HEARTS.COMMENT_ID.eq(commentId))
                .fetchInto(CommentHeartsEntity.class);

        final List<CommentHeart> hearts = heartEntities.stream()
                .map(h -> new CommentHeart(h.getId(), h.getCommentId(), h.getUserId(), h.getCreatedAt(), h.getDeletedAt()))
                .collect(Collectors.toCollection(ArrayList::new));

        return new Comment(
                entity.getId(),
                entity.getFeedId(),
                entity.getUserId(),
                parentCommentId,
                entity.getContent(),
                commentImages,
                commentVideos,
                new CommentHearts(hearts),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    // Batch fetch methods
    private Map<String, List<FeedImagesEntity>> fetchFeedImagesMap(final List<String> feedIds) {
        if (feedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return feedImagesDao.ctx()
                .selectFrom(FEED_IMAGES)
                .where(FEED_IMAGES.FEED_ID.in(feedIds))
                .fetchInto(FeedImagesEntity.class)
                .stream()
                .collect(Collectors.groupingBy(FeedImagesEntity::getFeedId));
    }

    private Map<String, List<FeedVideosEntity>> fetchFeedVideosMap(final List<String> feedIds) {
        if (feedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return feedVideosDao.ctx()
                .selectFrom(FEED_VIDEOS)
                .where(FEED_VIDEOS.FEED_ID.in(feedIds))
                .fetchInto(FeedVideosEntity.class)
                .stream()
                .collect(Collectors.groupingBy(FeedVideosEntity::getFeedId));
    }

    private Map<String, List<FeedHeartsEntity>> fetchFeedHeartsMap(final List<String> feedIds) {
        if (feedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return feedHeartsDao.ctx()
                .selectFrom(FEED_HEARTS)
                .where(FEED_HEARTS.FEED_ID.in(feedIds))
                .fetchInto(FeedHeartsEntity.class)
                .stream()
                .collect(Collectors.groupingBy(FeedHeartsEntity::getFeedId));
    }

    private Map<String, List<CommentRecord>> fetchFeedCommentsMap(final List<String> feedIds) {
        if (feedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return commentsDao.ctx()
                .select(COMMENTS.fields())
                .select(PARENT_COMMENT_ID)
                .from(COMMENTS)
                .where(COMMENTS.FEED_ID.in(feedIds))
                .fetch()
                .stream()
                .map(record -> new CommentRecord(
                        record.into(CommentsEntity.class),
                        record.get(PARENT_COMMENT_ID)
                ))
                .collect(Collectors.groupingBy(record -> record.entity.getFeedId()));
    }

    private Map<String, List<CommentImagesEntity>> fetchCommentImagesMap(final List<String> commentIds) {
        if (commentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return commentImagesDao.ctx()
                .selectFrom(COMMENT_IMAGES)
                .where(COMMENT_IMAGES.COMMENT_ID.in(commentIds))
                .fetchInto(CommentImagesEntity.class)
                .stream()
                .collect(Collectors.groupingBy(CommentImagesEntity::getCommentId));
    }

    private Map<String, List<CommentVideosEntity>> fetchCommentVideosMap(final List<String> commentIds) {
        if (commentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return commentVideosDao.ctx()
                .selectFrom(COMMENT_VIDEOS)
                .where(COMMENT_VIDEOS.COMMENT_ID.in(commentIds))
                .fetchInto(CommentVideosEntity.class)
                .stream()
                .collect(Collectors.groupingBy(CommentVideosEntity::getCommentId));
    }

    private Map<String, List<CommentHeartsEntity>> fetchCommentHeartsMap(final List<String> commentIds) {
        if (commentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return commentHeartsDao.ctx()
                .selectFrom(COMMENT_HEARTS)
                .where(COMMENT_HEARTS.COMMENT_ID.in(commentIds))
                .fetchInto(CommentHeartsEntity.class)
                .stream()
                .collect(Collectors.groupingBy(CommentHeartsEntity::getCommentId));
    }

    // Batched toDomain method
    private Feed toDomainWithBatchedData(
            final FeedsEntity entity,
            final String metadata,
            final Map<String, List<FeedImagesEntity>> feedImagesMap,
            final Map<String, List<FeedVideosEntity>> feedVideosMap,
            final Map<String, List<FeedHeartsEntity>> feedHeartsMap,
            final Map<String, List<CommentRecord>> feedCommentsMap,
            final Map<String, List<CommentImagesEntity>> commentImagesMap,
            final Map<String, List<CommentVideosEntity>> commentVideosMap,
            final Map<String, List<CommentHeartsEntity>> commentHeartsMap
    ) {
        final String feedId = entity.getId();

        // Feed Images
        final List<Image> images = feedImagesMap.getOrDefault(feedId, Collections.emptyList())
                .stream()
                .map(img -> new Image(img.getId(), img.getUrl(), img.getUserId(), img.getCreatedAt(), img.getDeletedAt()))
                .collect(Collectors.toCollection(ArrayList::new));
        final FeedImages feedImages = new FeedImages(feedId, images);

        // Feed Videos
        final List<Video> videos = feedVideosMap.getOrDefault(feedId, Collections.emptyList())
                .stream()
                .map(vid -> new Video(vid.getId(), vid.getUrl(), vid.getUserId(), vid.getCreatedAt(), vid.getDeletedAt()))
                .collect(Collectors.toCollection(ArrayList::new));
        final FeedVideos feedVideos = new FeedVideos(feedId, videos);

        // Comments with batched data
        final List<Comment> comments = feedCommentsMap.getOrDefault(feedId, Collections.emptyList())
                .stream()
                .map(record -> commentToDomainWithBatchedData(
                        record.entity,
                        record.parentCommentId,
                        commentImagesMap,
                        commentVideosMap,
                        commentHeartsMap
                ))
                .collect(Collectors.toCollection(ArrayList::new));

        // Hearts
        final List<Heart> hearts = feedHeartsMap.getOrDefault(feedId, Collections.emptyList())
                .stream()
                .map(h -> new Heart(h.getId(), h.getFeedId(), h.getUserId(), h.getCreatedAt(), h.getDeletedAt()))
                .collect(Collectors.toCollection(ArrayList::new));

        return new Feed(
                entity.getId(),
                entity.getClubId(),
                entity.getTitle(),
                entity.getContent(),
                FeedType.valueOf(entity.getFeedType()),
                metadata,
                feedImages,
                feedVideos,
                comments,
                hearts,
                entity.getUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private Comment commentToDomainWithBatchedData(
            final CommentsEntity entity,
            final String parentCommentId,
            final Map<String, List<CommentImagesEntity>> commentImagesMap,
            final Map<String, List<CommentVideosEntity>> commentVideosMap,
            final Map<String, List<CommentHeartsEntity>> commentHeartsMap
    ) {
        final String commentId = entity.getId();

        // Comment Images
        final List<Image> images = commentImagesMap.getOrDefault(commentId, Collections.emptyList())
                .stream()
                .map(img -> new Image(img.getId(), img.getUrl(), img.getUserId(), img.getCreatedAt(), img.getDeletedAt()))
                .collect(Collectors.toCollection(ArrayList::new));
        final CommentImages commentImages = new CommentImages(commentId, images);

        // Comment Videos
        final List<Video> videos = commentVideosMap.getOrDefault(commentId, Collections.emptyList())
                .stream()
                .map(vid -> new Video(vid.getId(), vid.getUrl(), vid.getUserId(), vid.getCreatedAt(), vid.getDeletedAt()))
                .collect(Collectors.toCollection(ArrayList::new));
        final CommentVideos commentVideos = new CommentVideos(commentId, videos);

        // Comment Hearts
        final List<CommentHeart> hearts = commentHeartsMap.getOrDefault(commentId, Collections.emptyList())
                .stream()
                .map(h -> new CommentHeart(h.getId(), h.getCommentId(), h.getUserId(), h.getCreatedAt(), h.getDeletedAt()))
                .collect(Collectors.toCollection(ArrayList::new));

        return new Comment(
                entity.getId(),
                entity.getFeedId(),
                entity.getUserId(),
                parentCommentId,
                entity.getContent(),
                commentImages,
                commentVideos,
                new CommentHearts(hearts),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    // Helper record to carry comment entity with parent_comment_id
    private record CommentRecord(CommentsEntity entity, String parentCommentId) {}
}
