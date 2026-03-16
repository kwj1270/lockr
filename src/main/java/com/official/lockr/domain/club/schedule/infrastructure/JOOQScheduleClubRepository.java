package com.official.lockr.domain.club.schedule.infrastructure;

import com.official.lockr.domain.club.club.domain.Member;
import com.official.lockr.domain.club.club.domain.MemberRole;
import com.official.lockr.domain.club.schedule.domain.ScheduleClub;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.MembersDao;
import org.jooq.generated.tables.pojos.MembersEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;

@Repository
public class JOOQScheduleClubRepository implements ScheduleClub {

    private final MembersDao membersDao;

    public JOOQScheduleClubRepository(final Configuration configuration) {
        this.membersDao = new MembersDao(configuration);
    }

    @Override
    public Member findMemberByUserIdAndClubId(final String userId, final String clubId) {
        final MembersEntity memberEntity = membersDao.ctx()
                .selectFrom(MEMBERS)
                .where(MEMBERS.USER_ID.eq(userId)
                        .and(MEMBERS.CLUB_ID.eq(clubId))
                        .and(MEMBERS.DELETED_AT.isNull()))
                .fetchOneInto(MembersEntity.class);

        return Optional.ofNullable(memberEntity)
                .map(this::toDomain)
                .orElse(null);
    }

    @Override
    public List<Member> findAllMemberIdsByClubId(final String clubId) {
        return membersDao.ctx()
                .selectFrom(MEMBERS)
                .where(
                        MEMBERS.CLUB_ID.eq(clubId),
                        MEMBERS.DELETED_AT.isNull()
                )
                .fetchInto(MembersEntity.class)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Member toDomain(final MembersEntity entity) {
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
}
