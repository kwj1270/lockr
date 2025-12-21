package com.official.lockr.domain.club.club.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.club.api.dto.MyClubResponse;
import com.official.lockr.domain.club.club.api.dto.MyClubsResponse;
import com.official.lockr.domain.club.club.api.dto.MyMemberInfoResponse;
import com.official.lockr.domain.club.club.domain.MemberRole;
import jakarta.servlet.http.HttpSession;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.ClubsDao;
import org.jooq.generated.tables.pojos.MembersEntity;
import org.jooq.impl.DSL;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.stream.Collectors.toMap;
import static org.jooq.generated.tables.ClubsJOOQEntity.CLUBS;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;

@RestController
@RequestMapping("/api/v1/clubs")
public class ClubQueryApi {

    private final ClubsDao clubsDao;

    public ClubQueryApi(final Configuration configuration) {
        this.clubsDao = new ClubsDao(configuration);
    }

    @GetMapping("/my")
    public ResponseEntity<MyClubsResponse> getMyClubs(
            final HttpSession httpSession,
            @RequestParam(value = "cursor", required = false, defaultValue = "") String cursor,
            @RequestParam(value = "limit", defaultValue = "5") int limit
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");

        var myClubDataQuery = clubsDao.ctx()
                .select(CLUBS.ID, MEMBERS.MEMBER_ROLE)
                .from(CLUBS)
                .innerJoin(MEMBERS).on(MEMBERS.CLUB_ID.eq(CLUBS.ID))
                .where(MEMBERS.USER_ID.eq(signIn.userId()))
                .and(CLUBS.DELETED_AT.isNull())
                .and(MEMBERS.DELETED_AT.isNull());

        if (!cursor.isEmpty()) {
            myClubDataQuery = myClubDataQuery.and(CLUBS.ID.lt(cursor));
        }

        final var myClubData = myClubDataQuery
                .orderBy(CLUBS.CREATED_AT.desc())
                .limit(limit)
                .fetch();

        if (myClubData.isEmpty()) {
            return ResponseEntity.ok(new MyClubsResponse(List.of()));
        }

        final var myClubIds = myClubData.stream()
                .map(record -> record.get(CLUBS.ID))
                .toList();

        final var clubIdToRole = myClubData.stream()
                .collect(toMap(
                        record -> record.get(CLUBS.ID),
                        record -> record.get(MEMBERS.MEMBER_ROLE)
                ));

        final List<MyClubResponse> clubs = clubsDao.ctx()
                .select(
                        CLUBS.ID,
                        CLUBS.NAME,
                        CLUBS.SPORT_TYPE,
                        CLUBS.CITY,
                        CLUBS.DISTRICT,
                        CLUBS.PROFILE_IMAGE_URL,
                        DSL.count(MEMBERS.ID).as("member_count")
                )
                .from(CLUBS)
                .join(MEMBERS).on(MEMBERS.CLUB_ID.eq(CLUBS.ID))
                .where(CLUBS.ID.in(myClubIds))
                .and(MEMBERS.DELETED_AT.isNull())
                .groupBy(CLUBS.ID, CLUBS.NAME, CLUBS.SPORT_TYPE, CLUBS.CITY, CLUBS.DISTRICT, CLUBS.PROFILE_IMAGE_URL)
                .orderBy(CLUBS.CREATED_AT.desc())
                .fetch()
                .map(record -> new MyClubResponse(
                        record.get(CLUBS.ID),
                        record.get(CLUBS.NAME),
                        record.get(CLUBS.SPORT_TYPE),
                        record.get("member_count", Integer.class),
                        record.get(CLUBS.CITY),
                        record.get(CLUBS.DISTRICT),
                        record.get(CLUBS.PROFILE_IMAGE_URL),
                        clubIdToRole.get(record.get(CLUBS.ID))
                ));

        return ResponseEntity.ok(new MyClubsResponse(clubs));
    }

    @GetMapping("/{clubId}/me")
    public ResponseEntity<MyMemberInfoResponse> getMyMemberInfo(
            final HttpSession httpSession,
            @PathVariable final String clubId
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");

        final MembersEntity member = clubsDao.ctx()
                .selectFrom(MEMBERS)
                .where(MEMBERS.CLUB_ID.eq(clubId))
                .and(MEMBERS.USER_ID.eq(signIn.userId()))
                .and(MEMBERS.DELETED_AT.isNull())
                .fetchOneInto(MembersEntity.class);

        if (member == null) {
            return ResponseEntity.notFound().build();
        }

        final MyMemberInfoResponse response = new MyMemberInfoResponse(
                member.getId(),
                member.getUserId(),
                member.getClubId(),
                MemberRole.valueOf(member.getMemberRole()),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );

        return ResponseEntity.ok(response);
    }
}
