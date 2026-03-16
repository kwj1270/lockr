package com.official.lockr.domain.notification.infrastructure;

import com.official.lockr.domain.notification.domain.NotificationTargetQuery;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.MembersDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;

@Repository
public class JOOQNotificationTargetQuery implements NotificationTargetQuery {

    private static final Set<String> STAFF_ROLES = Set.of(
            "PRESIDENT", "VICE_PRESIDENT", "MANAGER", "COACH", "TREASURER"
    );

    private final MembersDao membersDao;

    public JOOQNotificationTargetQuery(final Configuration configuration) {
        this.membersDao = new MembersDao(configuration);
    }

    @Override
    public List<String> findStaffUserIdsByClubId(final String clubId) {
        return membersDao.ctx()
                .select(MEMBERS.USER_ID)
                .from(MEMBERS)
                .where(MEMBERS.CLUB_ID.eq(clubId))
                .and(MEMBERS.MEMBER_ROLE.in(STAFF_ROLES))
                .and(MEMBERS.DELETED_AT.isNull())
                .fetchInto(String.class);
    }
}
