package com.official.lockr.domain.club.squad.infrastructure;

import com.official.lockr.domain.club.squad.domain.board.Staff;
import com.official.lockr.domain.club.squad.domain.board.Staffs;
import com.official.lockr.domain.club.team.domain.MemberRole;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.MembersDao;
import org.jooq.generated.tables.pojos.MembersEntity;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.jooq.generated.Tables.SQUADS;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;

@Component
public class JOOQStaffRepository implements Staffs {

    private final MembersDao membersDao;

    public JOOQStaffRepository(final Configuration configuration) {
        this.membersDao = new MembersDao(configuration);
    }

    @Nullable
    @Override
    public Staff find(final String squadId, final String userId) {
        final String memberUserId = membersDao.ctx()
                .select(MEMBERS.USER_ID)
                .from(MEMBERS)
                .join(SQUADS)
                .on(SQUADS.TEAM_ID.eq(MEMBERS.TEAM_ID))
                .where(
                        SQUADS.ID.eq(squadId),
                        MEMBERS.USER_ID.eq(userId),
                        MEMBERS.MEMBER_ROLE.in(
                                MemberRole.COACH.name(),
                                MemberRole.MANAGER.name()
                        ))
                .fetchOneInto(String.class);
        if (Objects.nonNull(memberUserId)) {
            return new Staff(memberUserId);
        }
        return null;
    }
}
