package com.official.lockr.domain.users.infrastructure;

import com.official.lockr.domain.users.domain.UserAdditionalInfo;
import com.official.lockr.domain.users.domain.Users;
import com.official.lockr.domain.users.domain.UsersRepository;
import com.official.lockr.global.ddd.DomainEventPublisher;
import com.official.lockr.global.vo.BirthDate;
import com.official.lockr.global.vo.Gender;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.UserAdditionalInfoDao;
import org.jooq.generated.tables.daos.UsersDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static org.jooq.generated.tables.UserAdditionalInfoJOOQEntity.USER_ADDITIONAL_INFO;
import static org.jooq.generated.tables.UsersJOOQEntity.USERS;
import static org.jooq.impl.DSL.excluded;

@Repository
public class JOOQUsersRepository implements UsersRepository {

    private final UsersDao usersDao;
    private final UserAdditionalInfoDao userAdditionalInfoDao;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQUsersRepository(final Configuration configuration,
                               final DomainEventPublisher domainEventPublisher) {
        this.usersDao = new UsersDao(configuration);
        this.userAdditionalInfoDao = new UserAdditionalInfoDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Transactional
    @Override
    public Users save(final Users users) {
        upsertUser(users);
        upsertUserAdditionalInfo(users.getUserAdditionalInfo());
        users.publish(domainEventPublisher);
        return users;
    }

    @Transactional(readOnly = true)
    @Override
    public Users findById(final String userId) {
        final var userRecord = usersDao.ctx()
                .selectFrom(USERS)
                .where(USERS.ID.eq(userId))
                .and(USERS.DELETED_AT.isNull())
                .fetchOne();

        if (userRecord == null) {
            return null;
        }

        final var additionalInfoRecord = userAdditionalInfoDao.ctx()
                .selectFrom(USER_ADDITIONAL_INFO)
                .where(USER_ADDITIONAL_INFO.USER_ID.eq(userId))
                .and(USER_ADDITIONAL_INFO.DELETED_AT.isNull())
                .fetchOne();

        if (additionalInfoRecord == null) {
            return null;
        }

        final UserAdditionalInfo userAdditionalInfo = new UserAdditionalInfo(
                additionalInfoRecord.getId(),
                additionalInfoRecord.getUserId(),
                additionalInfoRecord.getName(),
                Objects.nonNull(additionalInfoRecord.getBirthDate())? new BirthDate(additionalInfoRecord.getBirthDate()) : null,
                additionalInfoRecord.getPhone(),
                Gender.fromDbValue(additionalInfoRecord.getGender()),
                additionalInfoRecord.getProfileImage(),
                additionalInfoRecord.getCreatedAt(),
                additionalInfoRecord.getUpdatedAt(),
                additionalInfoRecord.getDeletedAt()
        );

        return new Users(
                userRecord.getId(),
                userAdditionalInfo,
                userRecord.getCreatedAt(),
                userRecord.getUpdatedAt(),
                userRecord.getDeletedAt()
        );
    }

    private void upsertUser(final Users users) {
        usersDao.ctx()
                .insertInto(USERS)
                .set(USERS.ID, users.getId())
                .set(USERS.CREATED_AT, users.getCreatedAt())
                .set(USERS.UPDATED_AT, users.getUpdatedAt())
                .set(USERS.DELETED_AT, users.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(USERS.UPDATED_AT, excluded(USERS.UPDATED_AT))
                .set(USERS.DELETED_AT, excluded(USERS.DELETED_AT))
                .execute();
    }

    private void upsertUserAdditionalInfo(final UserAdditionalInfo info) {
        userAdditionalInfoDao.ctx()
                .insertInto(USER_ADDITIONAL_INFO)
                .set(USER_ADDITIONAL_INFO.ID, info.getId())
                .set(USER_ADDITIONAL_INFO.USER_ID, info.getUserId())
                .set(USER_ADDITIONAL_INFO.NAME, info.getName())
                .set(USER_ADDITIONAL_INFO.BIRTH_DATE, info.getBirthDate())
                .set(USER_ADDITIONAL_INFO.PHONE, info.getPhone())
                .set(USER_ADDITIONAL_INFO.GENDER, Objects.nonNull(info.getGender()) ? info.getGender().toDbValue() : null)
                .set(USER_ADDITIONAL_INFO.PROFILE_IMAGE, info.getProfileImage())
                .set(USER_ADDITIONAL_INFO.CREATED_AT, info.getCreatedAt())
                .set(USER_ADDITIONAL_INFO.UPDATED_AT, info.getUpdatedAt())
                .set(USER_ADDITIONAL_INFO.DELETED_AT, info.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(USER_ADDITIONAL_INFO.NAME, excluded(USER_ADDITIONAL_INFO.NAME))
                .set(USER_ADDITIONAL_INFO.BIRTH_DATE, excluded(USER_ADDITIONAL_INFO.BIRTH_DATE))
                .set(USER_ADDITIONAL_INFO.PHONE, excluded(USER_ADDITIONAL_INFO.PHONE))
                .set(USER_ADDITIONAL_INFO.GENDER, excluded(USER_ADDITIONAL_INFO.GENDER))
                .set(USER_ADDITIONAL_INFO.PROFILE_IMAGE, excluded(USER_ADDITIONAL_INFO.PROFILE_IMAGE))
                .set(USER_ADDITIONAL_INFO.UPDATED_AT, excluded(USER_ADDITIONAL_INFO.UPDATED_AT))
                .set(USER_ADDITIONAL_INFO.DELETED_AT, excluded(USER_ADDITIONAL_INFO.DELETED_AT))
                .execute();
    }
}
