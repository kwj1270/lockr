package com.official.lockr.domain.club.club.infrastructure;

import com.official.lockr.domain.users.domain.ClubMembershipQuery;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.MembersDao;
import org.springframework.stereotype.Repository;

import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;

@Repository
public class JOOQClubMembershipQuery implements ClubMembershipQuery {

    private final MembersDao memberDao;

    public JOOQClubMembershipQuery(final Configuration configuration) {
        this.memberDao = new MembersDao(configuration);
    }

    @Override
    public boolean hasActiveClubMembership(final String userId) {
        return memberDao.ctx()
                .fetchExists(
                        memberDao.ctx()
                                .selectOne()
                                .from(MEMBERS)
                                .where(MEMBERS.USER_ID.eq(userId))
                                .and(MEMBERS.DELETED_AT.isNull())
                );
    }
}
