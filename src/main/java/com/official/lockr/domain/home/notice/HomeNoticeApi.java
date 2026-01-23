package com.official.lockr.domain.home.notice;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import jakarta.servlet.http.HttpSession;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.FeedsDao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static java.util.Objects.isNull;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.jooq.generated.tables.FeedsJOOQEntity.FEEDS;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;
import static org.jooq.generated.tables.ClubsJOOQEntity.CLUBS;
import static org.jooq.generated.tables.UserAdditionalInfoJOOQEntity.USER_ADDITIONAL_INFO;

@RequestMapping("/api/v1/home/notices")
@RestController
public class HomeNoticeApi {

    private final FeedsDao feedsDao;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public HomeNoticeApi(final Configuration configuration) {
        this.feedsDao = new FeedsDao(configuration);
    }

    @GetMapping
    public ResponseEntity<HomeNoticesResponse> notices(
            final HttpSession httpSession,
            @RequestParam(value = "limit", required = false) String limit
    ) {
        final SignInSession signIn = session(httpSession);

        final int limitCount = limit != null ? Integer.parseInt(limit) : 3;

        final List<String> userClubIds = feedsDao.ctx()
                .select(MEMBERS.CLUB_ID)
                .from(MEMBERS)
                .where(MEMBERS.USER_ID.eq(signIn.userId()))
                .and(MEMBERS.DELETED_AT.isNull())
                .fetch()
                .map(record -> record.get(MEMBERS.CLUB_ID));

        if (userClubIds.isEmpty()) {
            return ResponseEntity.ok(new HomeNoticesResponse(List.of()));
        }

        final List<HomeNoticeResponse> notices = feedsDao.ctx()
                .select(
                        FEEDS.ID,
                        FEEDS.CLUB_ID,
                        CLUBS.NAME.as("club_name"),
                        FEEDS.TITLE,
                        FEEDS.CONTENT,
                        FEEDS.USER_ID,
                        USER_ADDITIONAL_INFO.NAME.as("author_name"),
                        FEEDS.CREATED_AT
                )
                .from(FEEDS)
                .innerJoin(CLUBS).on(CLUBS.ID.eq(FEEDS.CLUB_ID))
                .leftJoin(USER_ADDITIONAL_INFO).on(USER_ADDITIONAL_INFO.USER_ID.eq(FEEDS.USER_ID))
                .where(FEEDS.CLUB_ID.in(userClubIds))
                .and(FEEDS.FEED_TYPE.eq("NOTICE"))
                .and(FEEDS.DELETED_AT.isNull())
                .orderBy(FEEDS.CREATED_AT.desc())
                .limit(limitCount)
                .fetch()
                .map(record -> {
                    final LocalDateTime createdAt = record.get(FEEDS.CREATED_AT);

                    return new HomeNoticeResponse(
                            record.get(FEEDS.ID),
                            record.get(FEEDS.CLUB_ID),
                            record.get("club_name", String.class),
                            record.get(FEEDS.TITLE),
                            record.get(FEEDS.CONTENT),
                            record.get(FEEDS.USER_ID),
                            record.get("author_name", String.class),
                            createdAt.format(FORMATTER)
                    );
                });

        return ResponseEntity.ok(new HomeNoticesResponse(notices));
    }

    record HomeNoticesResponse(
            List<HomeNoticeResponse> notices
    ) {
    }

    record HomeNoticeResponse(
            String id,
            String clubId,
            String clubName,
            String title,
            String content,
            String authorId,
            String authorName,
            String createdAt
    ) {
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(signIn)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return signIn;
    }
}
