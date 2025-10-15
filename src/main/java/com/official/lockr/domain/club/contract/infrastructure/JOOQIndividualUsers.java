package com.official.lockr.domain.club.contract.infrastructure;

import com.official.lockr.domain.club.contract.domain.IndividualUser;
import com.official.lockr.domain.club.contract.domain.IndividualUsers;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.UsersJOOQEntity;
import org.jooq.generated.tables.daos.UsersDao;
import org.springframework.stereotype.Component;

@Component
public class JOOQIndividualUsers implements IndividualUsers {

    private final UsersDao usersDao;

    public JOOQIndividualUsers(final Configuration configuration) {
        this.usersDao = new UsersDao(configuration);
    }

    @Nullable
    @Override
    public IndividualUser find(final String id) {
        return usersDao.ctx().select(UsersJOOQEntity.USERS.ID)
                .from(UsersJOOQEntity.USERS)
                .where(UsersJOOQEntity.USERS.ID.eq(id))
                .fetchOptional()
                .map(it -> new IndividualUser(it.get(UsersJOOQEntity.USERS.ID)))
                .orElse(null);
    }
}
