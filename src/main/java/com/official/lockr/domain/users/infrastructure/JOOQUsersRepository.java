package com.official.lockr.domain.users.infrastructure;

import com.official.lockr.domain.users.domain.Users;
import com.official.lockr.domain.users.domain.UsersRepository;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.UsersDao;
import org.jooq.generated.tables.pojos.UsersEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JOOQUsersRepository implements UsersRepository {

    private final UsersDao usersDao;

    public JOOQUsersRepository(final Configuration configuration) {
        this.usersDao = new UsersDao(configuration);
    }

    @Transactional
    @Override
    public Users save(final Users users) {
        final UsersEntity entity = new UsersEntity(
                users.getId(),
                users.getProviderId(),
                users.getProviderType(),
                users.getCreatedAt(),
                users.getUpdatedAt(),
                users.getDeletedAt()
        );
        usersDao.insert(entity);
        return new Users(
                entity.getId(),
                entity.getProviderId(),
                entity.getProviderType(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}
