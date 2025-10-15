package com.official.lockr.domain.club.contract.infrastructure;

import com.official.lockr.domain.club.contract.domain.Representative;
import com.official.lockr.domain.club.contract.domain.Representatives;
import com.official.lockr.domain.club.team.domain.MemberRole;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.MembersJOOQEntity;
import org.jooq.generated.tables.daos.MembersDao;
import org.jooq.generated.tables.records.MembersRecord;
import org.springframework.stereotype.Component;

@Component
public class JOOQRepresentativeRepository implements Representatives {

    private final MembersDao membersDao;

    public JOOQRepresentativeRepository(final Configuration configuration) {
        this.membersDao = new MembersDao(configuration);
    }

    @Nullable
    @Override
    public Representative find(final String teamId, final String userId) {
        return membersDao.ctx()
                .selectFrom(MembersJOOQEntity.MEMBERS)
                .where(
                        MembersJOOQEntity.MEMBERS.TEAM_ID.eq(teamId),
                        MembersJOOQEntity.MEMBERS.USER_ID.eq(userId),
                        MembersJOOQEntity.MEMBERS.MEMBER_ROLE.in(
                                MemberRole.PRESIDENT.name(),
                                MemberRole.VICE_PRESIDENT.name()
                        )
                ).fetchOptional()
                .map(JOOQRepresentativeRepository::manager)
                .orElse(null);
    }

    private static Representative manager(final MembersRecord record) {
        return new Representative(
                record.getUserId(),
                record.getMemberRole()
        );
    }
}
