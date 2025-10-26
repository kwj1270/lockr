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
import org.jooq.generated.tables.records.ClubsRecord;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.jooq.generated.tables.ClubsJOOQEntity.CLUBS;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;
import static org.jooq.impl.DSL.excluded;

@Repository
public class JOOQClubRepository implements ClubRepository {

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
        final ClubsRecord clubsRecord = clubsDao.ctx().selectFrom(CLUBS)
                .where(CLUBS.NAME.eq(name))
                .fetchOne();
        if (Objects.isNull(clubsRecord)) {
            return null;
        }
        return domain(clubsRecord, findAllMember(clubsRecord.getId()));
    }

    @Transactional
    @Override
    public Club save(final Club club) {
        upsertClub(club);
        syncMembers(club);
        club.publish(domainEventPublisher);
        return club;
    }

    // ✅ INSERT or UPDATE Club
    private void upsertClub(final Club club) {
        clubsDao.ctx()
                .insertInto(CLUBS)
                .set(CLUBS.ID, club.getId())
                .set(CLUBS.NAME, club.getName())
                .set(CLUBS.DESCRIPTION, club.getDescription())
                .set(CLUBS.CREATED_AT, club.getCreatedAt())
                .set(CLUBS.UPDATED_AT, club.getUpdatedAt())
                .set(CLUBS.DELETED_AT, club.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(CLUBS.NAME, club.getName())
                .set(CLUBS.DESCRIPTION, club.getDescription())
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
                    member.getCreatedAt(),
                    member.getUpdatedAt(),
                    member.getDeletedAt()
            );
        }

        query.onDuplicateKeyUpdate()
                .set(MEMBERS.USER_ID, excluded(MEMBERS.USER_ID))
                .set(MEMBERS.MEMBER_ROLE, excluded(MEMBERS.MEMBER_ROLE))
                .set(MEMBERS.UPDATED_AT, excluded(MEMBERS.UPDATED_AT))
                .set(MEMBERS.DELETED_AT, excluded(MEMBERS.DELETED_AT))
                .execute();
    }

    @Nullable
    @Override
    public Club findById(final String id) {
        final ClubsEntity teamsEntity = clubsDao.findById(id);
        if (Objects.isNull(teamsEntity)) {
            return null;
        }
        return domain(teamsEntity, findAllMember(id));
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

    private static Club domain(final ClubsEntity teamsEntity, final List<Member> members) {
        return new Club(
                teamsEntity.getId(),
                teamsEntity.getName(),
                teamsEntity.getDescription(),
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
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private Club domain(final ClubsRecord teamsRecord, final List<Member> members) {
        return new Club(
                teamsRecord.getId(),
                teamsRecord.getName(),
                teamsRecord.getDescription(),
                members,
                teamsRecord.getCreatedAt(),
                teamsRecord.getUpdatedAt(),
                teamsRecord.getDeletedAt()
        );
    }
}
