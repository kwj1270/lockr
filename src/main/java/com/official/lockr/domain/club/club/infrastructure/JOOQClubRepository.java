package com.official.lockr.domain.club.club.infrastructure;

import com.official.lockr.domain.club.club.domain.Club;
import com.official.lockr.domain.club.club.domain.ClubRepository;
import com.official.lockr.domain.club.club.domain.Member;
import com.official.lockr.domain.club.club.domain.MemberRole;
import com.official.lockr.global.ddd.DomainEventPublisher;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.ClubsDao;
import org.jooq.generated.tables.daos.MembersDao;
import org.jooq.generated.tables.pojos.ClubsEntity;
import org.jooq.generated.tables.pojos.MembersEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.jooq.generated.tables.ClubsJOOQEntity.CLUBS;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;
import static org.jooq.impl.DSL.excluded;
import static org.jooq.impl.DSL.field;

@Repository
public class JOOQClubRepository implements ClubRepository {

    private static final org.jooq.Field<Boolean> IS_PUBLIC = field("is_public", Boolean.class);
    private static final org.jooq.Field<String> JOIN_METHOD = field("join_method", String.class);

    private final ClubsDao clubsDao;
    private final MembersDao memberDao;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQClubRepository(final Configuration configuration,
                              final DomainEventPublisher domainEventPublisher
    ) {
        this.clubsDao = new ClubsDao(configuration);
        this.memberDao = new MembersDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Nullable
    @Override
    public Club findByName(final String name) {
        final var record = clubsDao.ctx().select(CLUBS.asterisk(), IS_PUBLIC, JOIN_METHOD)
                .from(CLUBS)
                .where(CLUBS.NAME.eq(name))
                .fetchOne();
        if (Objects.isNull(record)) {
            return null;
        }
        return domain(
                record.into(ClubsEntity.class),
                record.get(IS_PUBLIC, Boolean.class),
                record.get(JOIN_METHOD, String.class),
                findAllMember(record.get(CLUBS.ID))
        );
    }

    @Transactional
    @Override
    public Club save(final Club club) {
        upsertClub(club);
        syncMembers(club);
        club.publish(domainEventPublisher);
        return club;
    }

    private void upsertClub(final Club club) {
        clubsDao.ctx()
                .insertInto(CLUBS)
                .set(CLUBS.ID, club.getId())
                .set(CLUBS.FOUND_USER_ID, club.getFoundUserId())
                .set(CLUBS.NAME, club.getName())
                .set(CLUBS.SPORT_TYPE, club.getSportType())
                .set(CLUBS.CITY, club.getCity())
                .set(CLUBS.DISTRICT, club.getDistrict())
                .set(CLUBS.DESCRIPTION, club.getDescription())
                .set(CLUBS.PROFILE_IMAGE_URL, club.getProfileImageUrl())
                .set(CLUBS.BACKGROUND_IMAGE_URL, club.getBackgroundImageUrl())
                .set(IS_PUBLIC, club.isPublic())
                .set(JOIN_METHOD, club.getJoinMethod())
                .set(CLUBS.CREATED_AT, club.getCreatedAt())
                .set(CLUBS.UPDATED_AT, club.getUpdatedAt())
                .set(CLUBS.DELETED_AT, club.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(CLUBS.FOUND_USER_ID, club.getFoundUserId())
                .set(CLUBS.NAME, club.getName())
                .set(CLUBS.SPORT_TYPE, club.getSportType())
                .set(CLUBS.CITY, club.getCity())
                .set(CLUBS.DISTRICT, club.getDistrict())
                .set(CLUBS.DESCRIPTION, club.getDescription())
                .set(CLUBS.PROFILE_IMAGE_URL, club.getProfileImageUrl())
                .set(CLUBS.BACKGROUND_IMAGE_URL, club.getBackgroundImageUrl())
                .set(IS_PUBLIC, club.isPublic())
                .set(JOIN_METHOD, club.getJoinMethod())
                .set(CLUBS.UPDATED_AT, club.getUpdatedAt())
                .set(CLUBS.DELETED_AT, club.getDeletedAt())
                .execute();
    }

    // ✅ Member 동기화 (추가/수정/삭제)
    private void syncMembers(final Club club) {
        // 1. 기존 멤버 조회
        final List<String> existingMemberIds = memberDao.ctx()
                .select(MEMBERS.ID)
                .from(MEMBERS)
                .where(MEMBERS.CLUB_ID.eq(club.getId()))
                .fetchInto(String.class);

        final List<String> currentMemberIds = club.getMembers().stream()
                .map(Member::getId)
                .toList();

        // 2. 삭제할 멤버 (기존에는 있었지만 현재 없는)
        final List<String> membersToDelete = existingMemberIds.stream()
                .filter(id -> !currentMemberIds.contains(id))
                .toList();

        if (!membersToDelete.isEmpty()) {
            memberDao.ctx()
                    .deleteFrom(MEMBERS)
                    .where(MEMBERS.ID.in(membersToDelete))
                    .execute();
        }

        // 3. 추가/수정할 멤버 (UPSERT)
        if (!club.getMembers().isEmpty()) {
            upsertMembers(club.getMembers());
        }
    }

    private void upsertMembers(final List<Member> members) {
        if (members.isEmpty()) {
            return;
        }

        var query = memberDao.ctx().insertInto(MEMBERS,
                MEMBERS.ID,
                MEMBERS.USER_ID,
                MEMBERS.MEMBER_ROLE,
                MEMBERS.CLUB_ID,
                MEMBERS.NAME,
                MEMBERS.PROFILE_IMAGE,
                MEMBERS.CREATED_AT,
                MEMBERS.UPDATED_AT,
                MEMBERS.DELETED_AT
        );

        for (final Member member : members) {
            query.values(
                    member.getId(),
                    member.getUserId(),
                    member.getRole().name(),
                    member.getClubId(),
                    member.getName(),
                    member.getProfileImage(),
                    member.getCreatedAt(),
                    member.getUpdatedAt(),
                    member.getDeletedAt()
            );
        }

        query.onDuplicateKeyUpdate()
                .set(MEMBERS.USER_ID, excluded(MEMBERS.USER_ID))
                .set(MEMBERS.MEMBER_ROLE, excluded(MEMBERS.MEMBER_ROLE))
                .set(MEMBERS.NAME, excluded(MEMBERS.NAME))
                .set(MEMBERS.PROFILE_IMAGE, excluded(MEMBERS.PROFILE_IMAGE))
                .set(MEMBERS.UPDATED_AT, excluded(MEMBERS.UPDATED_AT))
                .set(MEMBERS.DELETED_AT, excluded(MEMBERS.DELETED_AT))
                .execute();
    }

    @Nullable
    @Override
    public Club findById(final String id) {
        final var record = clubsDao.ctx().select(CLUBS.asterisk(), IS_PUBLIC, JOIN_METHOD)
                .from(CLUBS)
                .where(CLUBS.ID.eq(id))
                .fetchOne();
        if (Objects.isNull(record)) {
            return null;
        }
        return domain(
                record.into(ClubsEntity.class),
                record.get(IS_PUBLIC, Boolean.class),
                record.get(JOIN_METHOD, String.class),
                findAllMember(id)
        );
    }

    private List<Member> findAllMember(final String teamId) {
        return memberDao.ctx()
                .selectFrom(MEMBERS)
                .where(MEMBERS.CLUB_ID.eq(teamId))
                .fetchInto(MembersEntity.class)
                .stream()
                .map(JOOQClubRepository::domain)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static Club domain(final ClubsEntity teamsEntity, final Boolean isPublic, final String joinMethod, final List<Member> members) {
        return new Club(
                teamsEntity.getId(),
                teamsEntity.getFoundUserId(),
                teamsEntity.getName(),
                teamsEntity.getSportType(),
                teamsEntity.getCity(),
                teamsEntity.getDistrict(),
                teamsEntity.getDescription(),
                teamsEntity.getProfileImageUrl(),
                teamsEntity.getBackgroundImageUrl(),
                isPublic != null ? isPublic : true,
                joinMethod != null ? joinMethod : "APPROVAL_REQUIRED",
                members,
                teamsEntity.getCreatedAt(),
                teamsEntity.getUpdatedAt(),
                teamsEntity.getDeletedAt()
        );
    }

    private static Member domain(final MembersEntity entity) {
        return new Member(
                entity.getId(),
                entity.getUserId(),
                MemberRole.valueOf(entity.getMemberRole()),
                entity.getClubId(),
                entity.getName(),
                entity.getProfileImage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}
