package com.official.lockr.domain.shorts.infrastructure;

import com.official.lockr.domain.shorts.domain.ShortsMember;
import com.official.lockr.domain.shorts.domain.ShortsClub;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.MembersDao;
import org.jooq.generated.tables.pojos.MembersEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;

@Repository
public class JOOQShortsClub implements ShortsClub {

    private static final Set<String> STAFF_ROLES = Set.of(
            "PRESIDENT", "VICE_PRESIDENT", "MANAGER", "COACH", "TREASURER"
    );

    private final MembersDao membersDao;

    public JOOQShortsClub(final Configuration configuration) {
        this.membersDao = new MembersDao(configuration);
    }

    @Nullable
    @Override
    public ShortsMember findMemberByUserIdAndClubId(final String userId, final String clubId) {
        final MembersEntity memberEntity = membersDao.ctx()
                .selectFrom(MEMBERS)
                .where(MEMBERS.USER_ID.eq(userId)
                        .and(MEMBERS.CLUB_ID.eq(clubId))
                        .and(MEMBERS.DELETED_AT.isNull()))
                .fetchOneInto(MembersEntity.class);

        return Optional.ofNullable(memberEntity)
                .map(entity -> new ShortsMember(
                        entity.getUserId(),
                        entity.getClubId(),
                        STAFF_ROLES.contains(entity.getMemberRole())
                ))
                .orElse(null);
    }
}
