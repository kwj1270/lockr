package com.official.lockr.domain.club.club.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.club.api.dto.*;
import com.official.lockr.domain.club.club.domain.MemberRole;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.ClubsDao;
import org.jooq.generated.tables.pojos.MembersEntity;
import org.jooq.impl.DSL;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.toMap;
import static org.jooq.generated.tables.ClubsJOOQEntity.CLUBS;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;
import static org.jooq.generated.tables.UserAdditionalInfoJOOQEntity.USER_ADDITIONAL_INFO;
import static org.jooq.generated.tables.SquadsJOOQEntity.SQUADS;
import static org.jooq.generated.tables.SquadPlayersJOOQEntity.SQUAD_PLAYERS;

@RestController
@RequestMapping("/api/v1/clubs")
public class ClubQueryApi {

    private final ClubsDao clubsDao;

    public ClubQueryApi(final Configuration configuration) {
        this.clubsDao = new ClubsDao(configuration);
    }

    @GetMapping
    public ResponseEntity<FindClubsResponse> clubs(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @RequestParam("name") final String name,
            @RequestParam("sportType") final String sportType
    ) {
        final FindClubsResponse findClubsResponses = new FindClubsResponse(clubsDao.ctx()
                .select(CLUBS)
                .from(CLUBS)
                .where(
                        CLUBS.NAME.eq(name),
                        CLUBS.SPORT_TYPE.eq("FOOT_BALL")
                )
                .and(CLUBS.DELETED_AT.isNull())
                .fetchInto(FindClubResponse.class));
        return ResponseEntity.ok(findClubsResponses);
    }

    @GetMapping("/my")
    public ResponseEntity<MyClubsResponse> getMyClubs(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @RequestParam(value = "cursor", required = false, defaultValue = "") String cursor,
            @RequestParam(value = "limit", defaultValue = "5") int limit
    ) {
        var myClubDataQuery = clubsDao.ctx()
                .select(CLUBS.ID, MEMBERS.MEMBER_ROLE)
                .from(CLUBS)
                .innerJoin(MEMBERS).on(MEMBERS.CLUB_ID.eq(CLUBS.ID))
                .where(MEMBERS.USER_ID.eq(signInSession.userId()))
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
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId
    ) {
        final MembersEntity member = clubsDao.ctx()
                .selectFrom(MEMBERS)
                .where(MEMBERS.CLUB_ID.eq(clubId))
                .and(MEMBERS.USER_ID.eq(signInSession.userId()))
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
                member.getProfileImage(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{clubId}/members")
    public ResponseEntity<MembersResponse> getMembers(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId
    ) {
        final var profileImageField = DSL.coalesce(MEMBERS.PROFILE_IMAGE, SQUAD_PLAYERS.PROFILE_IMAGE).as("profile_image");

        final List<MemberResponse> members = clubsDao.ctx()
                .select(
                        MEMBERS.USER_ID,
                        USER_ADDITIONAL_INFO.NAME,
                        MEMBERS.MEMBER_ROLE,
                        MEMBERS.CREATED_AT,
                        profileImageField,
                        SQUAD_PLAYERS.POSITIONS,
                        SQUAD_PLAYERS.BACK_NUMBER,
                        USER_ADDITIONAL_INFO.PHONE
                )
                .from(MEMBERS)
                .leftJoin(USER_ADDITIONAL_INFO).on(MEMBERS.USER_ID.eq(USER_ADDITIONAL_INFO.USER_ID))
                .leftJoin(SQUADS).on(SQUADS.CLUB_ID.eq(MEMBERS.CLUB_ID).and(SQUADS.DELETED_AT.isNull()))
                .leftJoin(SQUAD_PLAYERS).on(SQUAD_PLAYERS.SQUAD_ID.eq(SQUADS.ID)
                        .and(SQUAD_PLAYERS.USER_ID.eq(MEMBERS.USER_ID))
                        .and(SQUAD_PLAYERS.DELETED_AT.isNull()))
                .where(MEMBERS.CLUB_ID.eq(clubId))
                .and(MEMBERS.DELETED_AT.isNull())
                .orderBy(MEMBERS.CREATED_AT.asc())
                .fetch()
                .map(record -> new MemberResponse(
                        record.get(MEMBERS.USER_ID),
                        record.get(USER_ADDITIONAL_INFO.NAME),
                        record.get(MEMBERS.MEMBER_ROLE),
                        record.get(MEMBERS.CREATED_AT),
                        record.get("profile_image", String.class),
                        record.get(SQUAD_PLAYERS.POSITIONS),
                        record.get(SQUAD_PLAYERS.BACK_NUMBER),
                        record.get(USER_ADDITIONAL_INFO.PHONE)
                ));

        return ResponseEntity.ok(new MembersResponse(members));
    }
}
