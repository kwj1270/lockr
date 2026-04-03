package com.official.lockr.domain.home.card;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.UserPinnedClubsDao;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.jooq.generated.tables.ClubsJOOQEntity.CLUBS;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;
import static org.jooq.generated.tables.SchedulesJOOQEntity.SCHEDULES;
import static org.jooq.generated.tables.UserPinnedClubsJOOQEntity.USER_PINNED_CLUBS;
import static org.jooq.impl.DSL.min;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.selectCount;
import static org.jooq.impl.DSL.selectOne;

@RequestMapping("/api/v1/home/cards")
@RestController
public class HomeCardApi {

    private final UserPinnedClubsDao userPinnedClubsDao;

    public HomeCardApi(final Configuration configuration) {
        this.userPinnedClubsDao = new UserPinnedClubsDao(configuration);
    }

    @GetMapping
    public ResponseEntity<HomeCardsResponse> cards(@RequestAttribute("signInSession") final SignInSession signInSession) {
        var MEMBERS_COUNT = MEMBERS.as("m2");
        var memberCountField = selectCount()
                .from(MEMBERS_COUNT)
                .where(MEMBERS_COUNT.CLUB_ID.eq(CLUBS.ID)
                        .and(MEMBERS_COUNT.DELETED_AT.isNull()))
                .asField("member_count");

        var nextScheduleField = select(min(SCHEDULES.SCHEDULE_TIME))
                .from(SCHEDULES)
                .where(SCHEDULES.CLUB_ID.eq(CLUBS.ID)
                        .and(SCHEDULES.SCHEDULE_TIME.gt(LocalDateTime.now()))
                        .and(SCHEDULES.STATUS.eq("SCHEDULED")))
                .asField("next_schedule_date");

        final List<HomeCardResponse> cards = userPinnedClubsDao.ctx()
                .select(
                        CLUBS.ID,
                        CLUBS.NAME,
                        CLUBS.PROFILE_IMAGE_URL,
                        USER_PINNED_CLUBS.BACKGROUND_COLOR,
                        CLUBS.SPORT_TYPE,
                        MEMBERS.MEMBER_ROLE,
                        CLUBS.CITY,
                        CLUBS.DISTRICT,
                        memberCountField,
                        nextScheduleField
                )
                .from(USER_PINNED_CLUBS)
                .innerJoin(CLUBS).on(CLUBS.ID.eq(USER_PINNED_CLUBS.CLUB_ID))
                .innerJoin(MEMBERS).on(MEMBERS.CLUB_ID.eq(CLUBS.ID)
                        .and(MEMBERS.USER_ID.eq(signInSession.userId())))
                .where(USER_PINNED_CLUBS.USER_ID.eq(signInSession.userId()))
                .orderBy(USER_PINNED_CLUBS.PIN_ORDER.asc())
                .fetch()
                .map(record -> {
                    LocalDateTime nextSchedule = record.get("next_schedule_date", LocalDateTime.class);
                    return new HomeCardResponse(
                            record.get(CLUBS.ID),
                            record.get(CLUBS.NAME),
                            record.get(CLUBS.PROFILE_IMAGE_URL),
                            record.get(USER_PINNED_CLUBS.BACKGROUND_COLOR),
                            record.get(CLUBS.SPORT_TYPE),
                            record.get(MEMBERS.MEMBER_ROLE),
                            record.get("member_count", Integer.class),
                            record.get(CLUBS.CITY) + " " + record.get(CLUBS.DISTRICT),
                            nextSchedule != null ? nextSchedule.toString() : null
                    );
                });

        return ResponseEntity.ok(new HomeCardsResponse(cards));
    }

    @PostMapping("/pin")
    @Transactional
    public ResponseEntity<Void> pinClubs(
            @RequestBody PinClubsRequest request,
            @RequestAttribute("signInSession") final SignInSession signInSession
    ) {
        // 최대 2개 제한
        if (request.clubIds().size() > 2) {
            return ResponseEntity.badRequest().build();
        }

        // 가입하지 않은 클럽은 핀 설정 불가
        for (String clubId : request.clubIds()) {
            boolean isMember = userPinnedClubsDao.ctx()
                    .fetchExists(
                            selectOne()
                                    .from(MEMBERS)
                                    .where(MEMBERS.CLUB_ID.eq(clubId)
                                            .and(MEMBERS.USER_ID.eq(signInSession.userId()))
                                            .and(MEMBERS.DELETED_AT.isNull()))
                    );
            if (!isMember) {
                return ResponseEntity.badRequest().build();
            }
        }

        // 기존 핀 삭제
        userPinnedClubsDao.ctx()
                .deleteFrom(USER_PINNED_CLUBS)
                .where(USER_PINNED_CLUBS.USER_ID.eq(signInSession.userId()))
                .execute();

        // 새 핀 생성
        for (int i = 0; i < request.clubIds().size(); i++) {
            String clubId = request.clubIds().get(i);
            userPinnedClubsDao.ctx()
                    .insertInto(USER_PINNED_CLUBS)
                    .set(USER_PINNED_CLUBS.ID, UUID.randomUUID().toString())
                    .set(USER_PINNED_CLUBS.USER_ID, signInSession.userId())
                    .set(USER_PINNED_CLUBS.CLUB_ID, clubId)
                    .set(USER_PINNED_CLUBS.PIN_ORDER, i + 1)
                    .set(USER_PINNED_CLUBS.CREATED_AT, LocalDateTime.now())
                    .execute();
        }

        return ResponseEntity.ok().build();
    }

    record PinClubsRequest(List<String> clubIds) {}

    record HomeCardsResponse(List<HomeCardResponse> cards) {}

    record HomeCardResponse(
            String clubId,
            String name,
            String emblemUrl,
            String backgroundColor,
            String sport,
            String userRole,
            int memberCount,
            String location,
            String nextScheduleDate
    ) {}
}
