package com.official.lockr.domain.club.stats.infrastructure;

import com.official.lockr.domain.club.stats.domain.StatsClub;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.MembersDao;
import org.jooq.generated.tables.pojos.MembersEntity;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;

@Repository
public class JOOQStatsClub implements StatsClub {

    private static final Set<String> STAFF_ROLES = Set.of(
            "PRESIDENT", "VICE_PRESIDENT", "MANAGER", "COACH", "TREASURER"
    );

    private final MembersDao membersDao;

    public JOOQStatsClub(final Configuration configuration) {
        this.membersDao = new MembersDao(configuration);
    }

    @Override
    public void verifyStaffMembership(final String userId, final String clubId) {
        final MembersEntity member = membersDao.ctx()
                .selectFrom(MEMBERS)
                .where(MEMBERS.USER_ID.eq(userId))
                .and(MEMBERS.CLUB_ID.eq(clubId))
                .and(MEMBERS.DELETED_AT.isNull())
                .fetchOneInto(MembersEntity.class);

        if (member == null || !STAFF_ROLES.contains(member.getMemberRole())) {
            throw new IllegalArgumentException("Unauthorized access");
        }
    }
}
