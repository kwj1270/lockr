package com.official.lockr.domain.shorts.infrastructure;

import com.official.lockr.domain.shorts.domain.*;
import com.official.lockr.global.ddd.DomainEventPublisher;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.ShortsCommentsDao;
import org.jooq.generated.tables.daos.ShortsDao;
import org.jooq.generated.tables.daos.ShortsHeartsDao;
import org.jooq.generated.tables.daos.ShortsReportsDao;
import org.jooq.generated.tables.pojos.ShortsCommentsEntity;
import org.jooq.generated.tables.pojos.ShortsEntity;
import org.jooq.generated.tables.pojos.ShortsHeartsEntity;
import org.jooq.generated.tables.pojos.ShortsReportsEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.generated.tables.ShortsCommentsJOOQEntity.SHORTS_COMMENTS;
import static org.jooq.generated.tables.ShortsHeartsJOOQEntity.SHORTS_HEARTS;
import static org.jooq.generated.tables.ShortsJOOQEntity.SHORTS;
import static org.jooq.generated.tables.ShortsReportsJOOQEntity.SHORTS_REPORTS;
import static org.jooq.impl.DSL.excluded;

@Repository
public class JOOQShortsRepository implements ShortsRepository {

    private final ShortsDao shortsDao;
    private final ShortsHeartsDao shortsHeartsDao;
    private final ShortsCommentsDao shortsCommentsDao;
    private final ShortsReportsDao shortsReportsDao;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQShortsRepository(final Configuration configuration, final DomainEventPublisher domainEventPublisher) {
        this.shortsDao = new ShortsDao(configuration);
        this.shortsHeartsDao = new ShortsHeartsDao(configuration);
        this.shortsCommentsDao = new ShortsCommentsDao(configuration);
        this.shortsReportsDao = new ShortsReportsDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Nullable
    @Override
    public Shorts findById(final String id) {
        final ShortsEntity entity = shortsDao.ctx()
                .selectFrom(SHORTS)
                .where(SHORTS.ID.eq(id))
                .and(SHORTS.DELETED_AT.isNull())
                .fetchOneInto(ShortsEntity.class);

        if (entity == null) {
            return null;
        }

        return toDomain(entity);
    }

    @Transactional
    @Override
    public Shorts save(final Shorts shorts) {
        upsertShorts(shorts);
        syncHearts(shorts);
        syncComments(shorts);
        syncReports(shorts);
        shorts.publish(domainEventPublisher);
        return shorts;
    }

    private void upsertShorts(final Shorts shorts) {
        shortsDao.ctx()
                .insertInto(SHORTS)
                .set(SHORTS.ID, shorts.getId())
                .set(SHORTS.CLUB_ID, shorts.getClubId())
                .set(SHORTS.USER_ID, shorts.getUserId())
                .set(SHORTS.TITLE, shorts.getTitle())
                .set(SHORTS.DESCRIPTION, shorts.getDescription())
                .set(SHORTS.VIDEO_URL, shorts.getVideoUrl())
                .set(SHORTS.THUMBNAIL_URL, shorts.getThumbnailUrl())
                .set(SHORTS.DURATION, shorts.getDuration())
                .set(SHORTS.VIEW_COUNT, shorts.getViewCount())
                .set(SHORTS.MODERATION_STATUS, shorts.getModerationStatus().name())
                .set(SHORTS.CREATED_AT, shorts.getCreatedAt())
                .set(SHORTS.UPDATED_AT, shorts.getUpdatedAt())
                .set(SHORTS.DELETED_AT, shorts.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(SHORTS.TITLE, excluded(SHORTS.TITLE))
                .set(SHORTS.DESCRIPTION, excluded(SHORTS.DESCRIPTION))
                .set(SHORTS.VIEW_COUNT, excluded(SHORTS.VIEW_COUNT))
                .set(SHORTS.MODERATION_STATUS, excluded(SHORTS.MODERATION_STATUS))
                .set(SHORTS.UPDATED_AT, excluded(SHORTS.UPDATED_AT))
                .set(SHORTS.DELETED_AT, excluded(SHORTS.DELETED_AT))
                .execute();
    }

    private void syncHearts(final Shorts shorts) {
        for (final ShortsHeart heart : shorts.getHearts()) {
            shortsHeartsDao.ctx()
                    .insertInto(SHORTS_HEARTS)
                    .set(SHORTS_HEARTS.ID, heart.getId())
                    .set(SHORTS_HEARTS.SHORTS_ID, heart.getShortsId())
                    .set(SHORTS_HEARTS.USER_ID, heart.getUserId())
                    .set(SHORTS_HEARTS.CREATED_AT, heart.getCreatedAt())
                    .set(SHORTS_HEARTS.DELETED_AT, heart.getDeletedAt())
                    .onDuplicateKeyUpdate()
                    .set(SHORTS_HEARTS.DELETED_AT, excluded(SHORTS_HEARTS.DELETED_AT))
                    .execute();
        }
    }

    private void syncComments(final Shorts shorts) {
        for (final ShortsComment comment : shorts.getComments()) {
            shortsCommentsDao.ctx()
                    .insertInto(SHORTS_COMMENTS)
                    .set(SHORTS_COMMENTS.ID, comment.getId())
                    .set(SHORTS_COMMENTS.SHORTS_ID, comment.getShortsId())
                    .set(SHORTS_COMMENTS.USER_ID, comment.getUserId())
                    .set(SHORTS_COMMENTS.CONTENT, comment.getContent())
                    .set(SHORTS_COMMENTS.CREATED_AT, comment.getCreatedAt())
                    .set(SHORTS_COMMENTS.UPDATED_AT, comment.getUpdatedAt())
                    .set(SHORTS_COMMENTS.DELETED_AT, comment.getDeletedAt())
                    .onDuplicateKeyUpdate()
                    .set(SHORTS_COMMENTS.CONTENT, excluded(SHORTS_COMMENTS.CONTENT))
                    .set(SHORTS_COMMENTS.UPDATED_AT, excluded(SHORTS_COMMENTS.UPDATED_AT))
                    .set(SHORTS_COMMENTS.DELETED_AT, excluded(SHORTS_COMMENTS.DELETED_AT))
                    .execute();
        }
    }

    private void syncReports(final Shorts shorts) {
        for (final ShortsReport report : shorts.getReports()) {
            shortsReportsDao.ctx()
                    .insertInto(SHORTS_REPORTS)
                    .set(SHORTS_REPORTS.ID, report.getId())
                    .set(SHORTS_REPORTS.SHORTS_ID, report.getShortsId())
                    .set(SHORTS_REPORTS.USER_ID, report.getUserId())
                    .set(SHORTS_REPORTS.REASON, report.getReason().name())
                    .set(SHORTS_REPORTS.DETAIL, report.getDetail())
                    .set(SHORTS_REPORTS.CREATED_AT, report.getCreatedAt())
                    .onDuplicateKeyIgnore()
                    .execute();
        }
    }

    private Shorts toDomain(final ShortsEntity entity) {
        final String shortsId = entity.getId();

        final List<ShortsHeartsEntity> heartEntities = shortsHeartsDao.ctx()
                .selectFrom(SHORTS_HEARTS)
                .where(SHORTS_HEARTS.SHORTS_ID.eq(shortsId))
                .fetchInto(ShortsHeartsEntity.class);

        final List<ShortsHeart> hearts = heartEntities.stream()
                .map(h -> new ShortsHeart(h.getId(), h.getShortsId(), h.getUserId(), h.getCreatedAt(), h.getDeletedAt()))
                .collect(Collectors.toCollection(ArrayList::new));

        final List<ShortsCommentsEntity> commentEntities = shortsCommentsDao.ctx()
                .selectFrom(SHORTS_COMMENTS)
                .where(SHORTS_COMMENTS.SHORTS_ID.eq(shortsId))
                .fetchInto(ShortsCommentsEntity.class);

        final List<ShortsComment> comments = commentEntities.stream()
                .map(c -> new ShortsComment(c.getId(), c.getShortsId(), c.getUserId(), c.getContent(),
                        c.getCreatedAt(), c.getUpdatedAt(), c.getDeletedAt()))
                .collect(Collectors.toCollection(ArrayList::new));

        final List<ShortsReportsEntity> reportEntities = shortsReportsDao.ctx()
                .selectFrom(SHORTS_REPORTS)
                .where(SHORTS_REPORTS.SHORTS_ID.eq(shortsId))
                .fetchInto(ShortsReportsEntity.class);

        final List<ShortsReport> reports = reportEntities.stream()
                .map(r -> new ShortsReport(r.getId(), r.getShortsId(), r.getUserId(),
                        ReportReason.valueOf(r.getReason()), r.getDetail(), r.getCreatedAt()))
                .collect(Collectors.toCollection(ArrayList::new));

        return new Shorts(
                entity.getId(),
                entity.getClubId(),
                entity.getUserId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getVideoUrl(),
                entity.getThumbnailUrl(),
                entity.getDuration(),
                entity.getViewCount(),
                hearts,
                comments,
                ModerationStatus.valueOf(entity.getModerationStatus()),
                reports,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}
