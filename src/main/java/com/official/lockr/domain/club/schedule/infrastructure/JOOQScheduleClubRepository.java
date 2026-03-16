package com.official.lockr.domain.club.schedule.infrastructure;

import com.official.lockr.domain.club.schedule.domain.ScheduleClub;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.ClubsDao;
import org.jooq.generated.tables.daos.MembersDao;
import org.jooq.generated.tables.pojos.MembersEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static org.jooq.generated.tables.ClubsJOOQEntity.CLUBS;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;

@Repository
public class JOOQScheduleClubRepository implements ScheduleClub {

    private static final Set<String> STAFF_ROLES = Set.of(
            "PRESIDENT", "VICE_PRESIDENT", "MANAGER", "COACH", "TREASURER"
    );

    private final MembersDao membersDao;
    private final ClubsDao clubsDao;

    public JOOQScheduleClubRepository(final Configuration configuration) {
        this.membersDao = new MembersDao(configuration);
        this.clubsDao = new ClubsDao(configuration);
    }

    @Override
    public boolean isStaff(final String userId, final String clubId) {
        final MembersEntity member = membersDao.ctx()
                .selectFrom(MEMBERS)
                .where(MEMBERS.USER_ID.eq(userId)
                        .and(MEMBERS.CLUB_ID.eq(clubId))
                        .and(MEMBERS.DELETED_AT.isNull()))
                .fetchOneInto(MembersEntity.class);

        if (member == null) {
            return false;
        }
        return STAFF_ROLES.contains(member.getMemberRole());
    }

    @Override
    public boolean isMember(final String userId, final String clubId) {
        return membersDao.ctx()
                .fetchCount(MEMBERS,
                        MEMBERS.USER_ID.eq(userId)
                                .and(MEMBERS.CLUB_ID.eq(clubId))
                                .and(MEMBERS.DELETED_AT.isNull())
                ) > 0;
    }

    @Override
    public String findStaffRoleName(final String userId, final String clubId) {
        final MembersEntity member = membersDao.ctx()
                .selectFrom(MEMBERS)
                .where(MEMBERS.USER_ID.eq(userId)
                        .and(MEMBERS.CLUB_ID.eq(clubId))
                        .and(MEMBERS.DELETED_AT.isNull()))
                .fetchOneInto(MembersEntity.class);

        if (member == null) {
            return null;
        }
        return STAFF_ROLES.contains(member.getMemberRole()) ? member.getMemberRole() : null;
    }

    @Override
    public String findClubNameById(final String clubId) {
        return clubsDao.ctx()
                .select(CLUBS.NAME)
                .from(CLUBS)
                .where(CLUBS.ID.eq(clubId)
                        .and(CLUBS.DELETED_AT.isNull()))
                .fetchOneInto(String.class);
    }

    @Override
    public boolean existsClub(final String clubId) {
        return clubsDao.ctx()
                .fetchCount(CLUBS,
                        CLUBS.ID.eq(clubId)
                                .and(CLUBS.DELETED_AT.isNull())
                ) > 0;
    }

    @Override
    public List<String> findAllUserIdsByClubId(final String clubId) {
        return membersDao.ctx()
                .select(MEMBERS.USER_ID)
                .from(MEMBERS)
                .where(MEMBERS.CLUB_ID.eq(clubId)
                        .and(MEMBERS.DELETED_AT.isNull()))
                .fetchInto(String.class);
    }
}
