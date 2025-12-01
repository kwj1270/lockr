package com.official.lockr.domain.club.lineup.tacticalboard.infrastructure;

import com.official.lockr.domain.club.lineup.tacticalboard.domain.Coach;
import com.official.lockr.domain.club.lineup.tacticalboard.domain.CoachingStaff;
import com.official.lockr.domain.club.club.domain.MemberRole;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.MembersDao;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.jooq.generated.Tables.SQUADS;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;

@Component
public class JOOQCoachingStaffRepository implements CoachingStaff {

    private final MembersDao membersDao;

    public JOOQCoachingStaffRepository(final Configuration configuration) {
        this.membersDao = new MembersDao(configuration);
    }

    @Nullable
    @Override
    public Coach find(final String clubId, final String userId) {
        final String memberUserId = membersDao.ctx()
                .select(MEMBERS.USER_ID)
                .from(MEMBERS)
                .join(SQUADS)
                .on(SQUADS.CLUB_ID.eq(clubId))
                .where(
                        MEMBERS.USER_ID.eq(userId),
                        MEMBERS.MEMBER_ROLE.in(
                                MemberRole.COACH.name(),
                                MemberRole.MANAGER.name()
                        ))
                .fetchOneInto(String.class);
        if (Objects.nonNull(memberUserId)) {
            return new Coach(memberUserId);
        }
        return null;
    }
}
