package com.official.lockr.domain.home.notice;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import jakarta.servlet.http.HttpSession;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.FeedsDao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static java.util.Objects.isNull;
import static org.jooq.generated.tables.ClubsJOOQEntity.CLUBS;
import static org.jooq.generated.tables.FeedsJOOQEntity.FEEDS;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;

@RequestMapping("/api/v1/home/notices")
@RestController
public class HomeNoticeApi {

    private final FeedsDao feedsDao;

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
                        FEEDS.CONTENT
                )
                .from(FEEDS)
                .innerJoin(CLUBS).on(CLUBS.ID.eq(FEEDS.CLUB_ID))
                .where(FEEDS.CLUB_ID.in(userClubIds))
                .and(FEEDS.FEED_TYPE.eq("NOTICE"))
                .and(FEEDS.DELETED_AT.isNull())
                .orderBy(FEEDS.CREATED_AT.desc())
                .limit(limitCount)
                .fetch()
                .map(record -> new HomeNoticeResponse(
                        record.get(FEEDS.ID),
                        record.get(FEEDS.CLUB_ID),
                        record.get("club_name", String.class),
                        record.get(FEEDS.TITLE),
                        record.get(FEEDS.CONTENT)
                ));

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
            String content
    ) {
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute(SignInSession.SESSION_KEY);
        if (isNull(signIn)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return signIn;
    }
}
