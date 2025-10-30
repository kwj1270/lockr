package com.official.lockr.domain.club.match.infrastructure;

import com.official.lockr.domain.club.club.domain.MemberRole;
import com.official.lockr.domain.club.match.domain.ClubManager;
import com.official.lockr.domain.club.match.domain.ClubManagers;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.MembersDao;
import org.springframework.stereotype.Component;

import static java.util.Objects.nonNull;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;

@Component
public class JOOQManagerRepository implements ClubManagers {

    private final MembersDao membersDao;

    public JOOQManagerRepository(final Configuration configuration) {
        this.membersDao = new MembersDao(configuration);
    }

    @Nullable
    @Override
    public ClubManager find(final String clubId, final String clubManagerUserId) {
        final String memberUserId = membersDao.ctx()
                .select(MEMBERS.USER_ID)
                .from(MEMBERS)
                .where(
                        MEMBERS.CLUB_ID.eq(clubId),
                        MEMBERS.USER_ID.eq(clubManagerUserId),
                        MEMBERS.MEMBER_ROLE.in(
                                MemberRole.COACH.name(),
                                MemberRole.MANAGER.name()
                        )
                )
                .fetchOneInto(String.class);

        if (nonNull(memberUserId)) {
            return new ClubManager(memberUserId, clubId);
        }
        return null;
    }
}
