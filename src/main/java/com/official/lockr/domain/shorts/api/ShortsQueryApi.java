package com.official.lockr.domain.shorts.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.shorts.api.dto.*;
import org.jooq.Configuration;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.generated.tables.daos.ShortsDao;
import org.jooq.impl.DSL;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.jooq.generated.tables.ClubsJOOQEntity.CLUBS;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;
import static org.jooq.generated.tables.ShortsCommentsJOOQEntity.SHORTS_COMMENTS;
import static org.jooq.generated.tables.ShortsHeartsJOOQEntity.SHORTS_HEARTS;
import static org.jooq.generated.tables.ShortsJOOQEntity.SHORTS;
import static org.jooq.generated.tables.ShortsReportsJOOQEntity.SHORTS_REPORTS;
import static org.jooq.generated.tables.UserAdditionalInfoJOOQEntity.USER_ADDITIONAL_INFO;

@RestController
@RequestMapping("/api/v1/shorts")
public class ShortsQueryApi {

    private final ShortsDao shortsDao;

    public ShortsQueryApi(final Configuration configuration) {
        this.shortsDao = new ShortsDao(configuration);
    }

    @GetMapping
    public ResponseEntity<ShortsListResponse> getShortsFeed(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @RequestParam(value = "cursor", required = false, defaultValue = "") final String cursor,
            @RequestParam(value = "limit", defaultValue = "20") final int limit
    ) {
        final var heartsCount = DSL.selectCount()
                .from(SHORTS_HEARTS)
                .where(SHORTS_HEARTS.SHORTS_ID.eq(SHORTS.ID))
                .and(SHORTS_HEARTS.DELETED_AT.isNull())
                .asField("hearts_count");

        final var commentsCount = DSL.selectCount()
                .from(SHORTS_COMMENTS)
                .where(SHORTS_COMMENTS.SHORTS_ID.eq(SHORTS.ID))
                .and(SHORTS_COMMENTS.DELETED_AT.isNull())
                .asField("comments_count");

        final var isHearted = DSL.selectCount()
                .from(SHORTS_HEARTS)
                .where(SHORTS_HEARTS.SHORTS_ID.eq(SHORTS.ID))
                .and(SHORTS_HEARTS.USER_ID.eq(signInSession.userId()))
                .and(SHORTS_HEARTS.DELETED_AT.isNull())
                .asField("is_hearted");

        var query = shortsDao.ctx()
                .select(
                        SHORTS.ID, SHORTS.CLUB_ID, SHORTS.USER_ID,
                        SHORTS.TITLE, SHORTS.VIDEO_URL, SHORTS.THUMBNAIL_URL,
                        SHORTS.DURATION, SHORTS.VIEW_COUNT, SHORTS.CREATED_AT,
                        CLUBS.NAME.as("club_name"),
                        USER_ADDITIONAL_INFO.NAME.as("user_name"),
                        MEMBERS.PROFILE_IMAGE.as("user_profile_image"),
                        heartsCount, commentsCount, isHearted
                )
                .from(SHORTS)
                .leftJoin(CLUBS).on(CLUBS.ID.eq(SHORTS.CLUB_ID))
                .leftJoin(MEMBERS).on(MEMBERS.USER_ID.eq(SHORTS.USER_ID)
                        .and(MEMBERS.CLUB_ID.eq(SHORTS.CLUB_ID))
                        .and(MEMBERS.DELETED_AT.isNull()))
                .leftJoin(USER_ADDITIONAL_INFO).on(USER_ADDITIONAL_INFO.USER_ID.eq(SHORTS.USER_ID))
                .where(SHORTS.DELETED_AT.isNull())
                .and(SHORTS.MODERATION_STATUS.eq("ACTIVE"));

        if (!cursor.isEmpty()) {
            query = query.and(SHORTS.ID.lt(cursor));
        }

        final List<ShortsItemResponse> items = query
                .orderBy(SHORTS.CREATED_AT.desc())
                .limit(limit)
                .fetch()
                .map(record -> mapToItemResponse(record));

        return ResponseEntity.ok(new ShortsListResponse(items));
    }

    @GetMapping("/my-clubs")
    public ResponseEntity<ShortsListResponse> getMyClubsShortsFeed(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @RequestParam(value = "cursor", required = false, defaultValue = "") final String cursor,
            @RequestParam(value = "limit", defaultValue = "20") final int limit
    ) {
        final var myClubIds = shortsDao.ctx()
                .select(MEMBERS.CLUB_ID)
                .from(MEMBERS)
                .where(MEMBERS.USER_ID.eq(signInSession.userId()))
                .and(MEMBERS.DELETED_AT.isNull())
                .fetchInto(String.class);

        final var heartsCount = DSL.selectCount()
                .from(SHORTS_HEARTS)
                .where(SHORTS_HEARTS.SHORTS_ID.eq(SHORTS.ID))
                .and(SHORTS_HEARTS.DELETED_AT.isNull())
                .asField("hearts_count");

        final var commentsCount = DSL.selectCount()
                .from(SHORTS_COMMENTS)
                .where(SHORTS_COMMENTS.SHORTS_ID.eq(SHORTS.ID))
                .and(SHORTS_COMMENTS.DELETED_AT.isNull())
                .asField("comments_count");

        final var isHearted = DSL.selectCount()
                .from(SHORTS_HEARTS)
                .where(SHORTS_HEARTS.SHORTS_ID.eq(SHORTS.ID))
                .and(SHORTS_HEARTS.USER_ID.eq(signInSession.userId()))
                .and(SHORTS_HEARTS.DELETED_AT.isNull())
                .asField("is_hearted");

        final var myClubPriority = DSL.when(SHORTS.CLUB_ID.in(myClubIds), 0).otherwise(1);

        var query = shortsDao.ctx()
                .select(
                        SHORTS.ID, SHORTS.CLUB_ID, SHORTS.USER_ID,
                        SHORTS.TITLE, SHORTS.VIDEO_URL, SHORTS.THUMBNAIL_URL,
                        SHORTS.DURATION, SHORTS.VIEW_COUNT, SHORTS.CREATED_AT,
                        CLUBS.NAME.as("club_name"),
                        USER_ADDITIONAL_INFO.NAME.as("user_name"),
                        MEMBERS.PROFILE_IMAGE.as("user_profile_image"),
                        heartsCount, commentsCount, isHearted
                )
                .from(SHORTS)
                .leftJoin(CLUBS).on(CLUBS.ID.eq(SHORTS.CLUB_ID))
                .leftJoin(MEMBERS).on(MEMBERS.USER_ID.eq(SHORTS.USER_ID)
                        .and(MEMBERS.CLUB_ID.eq(SHORTS.CLUB_ID))
                        .and(MEMBERS.DELETED_AT.isNull()))
                .leftJoin(USER_ADDITIONAL_INFO).on(USER_ADDITIONAL_INFO.USER_ID.eq(SHORTS.USER_ID))
                .where(SHORTS.DELETED_AT.isNull())
                .and(SHORTS.MODERATION_STATUS.eq("ACTIVE"));

        if (!cursor.isEmpty()) {
            query = query.and(SHORTS.ID.lt(cursor));
        }

        final List<ShortsItemResponse> items = query
                .orderBy(myClubPriority.asc(), SHORTS.CREATED_AT.desc())
                .limit(limit)
                .fetch()
                .map(record -> mapToItemResponse(record));

        return ResponseEntity.ok(new ShortsListResponse(items));
    }

    @GetMapping("/{shortsId}")
    public ResponseEntity<ShortsDetailResponse> getShortsDetail(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String shortsId
    ) {
        final var heartsCount = DSL.selectCount()
                .from(SHORTS_HEARTS)
                .where(SHORTS_HEARTS.SHORTS_ID.eq(SHORTS.ID))
                .and(SHORTS_HEARTS.DELETED_AT.isNull())
                .asField("hearts_count");

        final var commentsCount = DSL.selectCount()
                .from(SHORTS_COMMENTS)
                .where(SHORTS_COMMENTS.SHORTS_ID.eq(SHORTS.ID))
                .and(SHORTS_COMMENTS.DELETED_AT.isNull())
                .asField("comments_count");

        final var isHearted = DSL.selectCount()
                .from(SHORTS_HEARTS)
                .where(SHORTS_HEARTS.SHORTS_ID.eq(SHORTS.ID))
                .and(SHORTS_HEARTS.USER_ID.eq(signInSession.userId()))
                .and(SHORTS_HEARTS.DELETED_AT.isNull())
                .asField("is_hearted");

        final var record = shortsDao.ctx()
                .select(
                        SHORTS.ID, SHORTS.CLUB_ID, SHORTS.USER_ID,
                        SHORTS.TITLE, SHORTS.DESCRIPTION, SHORTS.VIDEO_URL, SHORTS.THUMBNAIL_URL,
                        SHORTS.DURATION, SHORTS.VIEW_COUNT, SHORTS.CREATED_AT,
                        CLUBS.NAME.as("club_name"),
                        USER_ADDITIONAL_INFO.NAME.as("user_name"),
                        MEMBERS.PROFILE_IMAGE.as("user_profile_image"),
                        heartsCount, commentsCount, isHearted
                )
                .from(SHORTS)
                .leftJoin(CLUBS).on(CLUBS.ID.eq(SHORTS.CLUB_ID))
                .leftJoin(MEMBERS).on(MEMBERS.USER_ID.eq(SHORTS.USER_ID)
                        .and(MEMBERS.CLUB_ID.eq(SHORTS.CLUB_ID))
                        .and(MEMBERS.DELETED_AT.isNull()))
                .leftJoin(USER_ADDITIONAL_INFO).on(USER_ADDITIONAL_INFO.USER_ID.eq(SHORTS.USER_ID))
                .where(SHORTS.ID.eq(shortsId))
                .and(SHORTS.DELETED_AT.isNull())
                .and(SHORTS.MODERATION_STATUS.eq("ACTIVE"))
                .fetchOne();

        if (record == null) {
            return ResponseEntity.notFound().build();
        }

        final List<ShortsCommentResponse> comments = shortsDao.ctx()
                .select(
                        SHORTS_COMMENTS.ID, SHORTS_COMMENTS.USER_ID,
                        SHORTS_COMMENTS.CONTENT, SHORTS_COMMENTS.CREATED_AT,
                        USER_ADDITIONAL_INFO.NAME.as("user_name"),
                        MEMBERS.PROFILE_IMAGE.as("user_profile_image")
                )
                .from(SHORTS_COMMENTS)
                .leftJoin(USER_ADDITIONAL_INFO).on(USER_ADDITIONAL_INFO.USER_ID.eq(SHORTS_COMMENTS.USER_ID))
                .leftJoin(MEMBERS).on(MEMBERS.USER_ID.eq(SHORTS_COMMENTS.USER_ID)
                        .and(MEMBERS.CLUB_ID.eq(record.get(SHORTS.CLUB_ID)))
                        .and(MEMBERS.DELETED_AT.isNull()))
                .where(SHORTS_COMMENTS.SHORTS_ID.eq(shortsId))
                .and(SHORTS_COMMENTS.DELETED_AT.isNull())
                .orderBy(SHORTS_COMMENTS.CREATED_AT.asc())
                .fetch()
                .map(r -> new ShortsCommentResponse(
                        r.get(SHORTS_COMMENTS.ID),
                        r.get(SHORTS_COMMENTS.USER_ID),
                        r.get("user_name", String.class),
                        r.get("user_profile_image", String.class),
                        r.get(SHORTS_COMMENTS.CONTENT),
                        r.get(SHORTS_COMMENTS.CREATED_AT)
                ));

        return ResponseEntity.ok(new ShortsDetailResponse(
                record.get(SHORTS.ID),
                record.get(SHORTS.CLUB_ID),
                record.get("club_name", String.class),
                record.get(SHORTS.USER_ID),
                record.get("user_name", String.class),
                record.get("user_profile_image", String.class),
                record.get(SHORTS.TITLE),
                record.get(SHORTS.DESCRIPTION),
                record.get(SHORTS.VIDEO_URL),
                record.get(SHORTS.THUMBNAIL_URL),
                record.get(SHORTS.DURATION),
                record.get(SHORTS.VIEW_COUNT),
                record.get("hearts_count", Integer.class),
                record.get("comments_count", Integer.class),
                record.get("is_hearted", Integer.class) > 0,
                comments,
                record.get(SHORTS.CREATED_AT)
        ));
    }

    @PostMapping("/{shortsId}/view")
    public ResponseEntity<Void> incrementViewCount(
            @PathVariable final String shortsId
    ) {
        shortsDao.ctx()
                .update(SHORTS)
                .set(SHORTS.VIEW_COUNT, SHORTS.VIEW_COUNT.add(1))
                .where(SHORTS.ID.eq(shortsId))
                .and(SHORTS.DELETED_AT.isNull())
                .execute();

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{shortsId}/reports")
    public ResponseEntity<List<ShortsReportResponse>> getReports(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String shortsId
    ) {
        final var shortsRecord = shortsDao.ctx()
                .select(SHORTS.CLUB_ID)
                .from(SHORTS)
                .where(SHORTS.ID.eq(shortsId))
                .and(SHORTS.DELETED_AT.isNull())
                .fetchOne();

        if (shortsRecord == null) {
            return ResponseEntity.notFound().build();
        }

        final String clubId = shortsRecord.get(SHORTS.CLUB_ID);

        final var member = shortsDao.ctx()
                .selectFrom(MEMBERS)
                .where(MEMBERS.USER_ID.eq(signInSession.userId()))
                .and(MEMBERS.CLUB_ID.eq(clubId))
                .and(MEMBERS.DELETED_AT.isNull())
                .fetchOne();

        if (member == null || !isStaff(member.getMemberRole())) {
            return ResponseEntity.status(403).build();
        }

        final List<ShortsReportResponse> reports = shortsDao.ctx()
                .select(
                        SHORTS_REPORTS.ID, SHORTS_REPORTS.USER_ID,
                        SHORTS_REPORTS.REASON, SHORTS_REPORTS.DETAIL,
                        SHORTS_REPORTS.CREATED_AT,
                        USER_ADDITIONAL_INFO.NAME.as("user_name")
                )
                .from(SHORTS_REPORTS)
                .leftJoin(USER_ADDITIONAL_INFO).on(USER_ADDITIONAL_INFO.USER_ID.eq(SHORTS_REPORTS.USER_ID))
                .where(SHORTS_REPORTS.SHORTS_ID.eq(shortsId))
                .orderBy(SHORTS_REPORTS.CREATED_AT.desc())
                .fetch()
                .map(r -> new ShortsReportResponse(
                        r.get(SHORTS_REPORTS.ID),
                        r.get(SHORTS_REPORTS.USER_ID),
                        r.get("user_name", String.class),
                        r.get(SHORTS_REPORTS.REASON),
                        r.get(SHORTS_REPORTS.DETAIL),
                        r.get(SHORTS_REPORTS.CREATED_AT)
                ));

        return ResponseEntity.ok(reports);
    }

    private static boolean isStaff(final String role) {
        return "PRESIDENT".equals(role) || "MANAGER".equals(role) || "COACH".equals(role);
    }

    private ShortsItemResponse mapToItemResponse(final Record record) {
        return new ShortsItemResponse(
                record.get(SHORTS.ID),
                record.get(SHORTS.CLUB_ID),
                record.get("club_name", String.class),
                record.get(SHORTS.USER_ID),
                record.get("user_name", String.class),
                record.get("user_profile_image", String.class),
                record.get(SHORTS.TITLE),
                record.get(SHORTS.VIDEO_URL),
                record.get(SHORTS.THUMBNAIL_URL),
                record.get(SHORTS.DURATION),
                record.get(SHORTS.VIEW_COUNT),
                record.get("hearts_count", Integer.class),
                record.get("comments_count", Integer.class),
                record.get("is_hearted", Integer.class) > 0,
                record.get(SHORTS.CREATED_AT)
        );
    }
}
