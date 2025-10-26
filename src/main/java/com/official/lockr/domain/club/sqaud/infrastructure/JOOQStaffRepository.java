package com.official.lockr.domain.club.sqaud.infrastructure;

import com.official.lockr.domain.club.sqaud.domain.board.Staff;
import com.official.lockr.domain.club.sqaud.domain.board.Staffs;
import com.official.lockr.domain.club.club.domain.MemberRole;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.MembersDao;
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
                .on(SQUADS.CLUB_ID.eq(MEMBERS.CLUB_ID))
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
