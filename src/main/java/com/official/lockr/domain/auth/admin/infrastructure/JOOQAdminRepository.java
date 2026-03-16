package com.official.lockr.domain.auth.admin.infrastructure;

import com.official.lockr.domain.auth.admin.domain.Admin;
import com.official.lockr.domain.auth.admin.domain.AdminRepository;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.AdminDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static org.jooq.generated.Tables.ADMIN;
import static org.jooq.impl.DSL.excluded;

@Repository
public class JOOQAdminRepository implements AdminRepository {

    private final AdminDao adminDao;

    public JOOQAdminRepository(final Configuration configuration) {
        this.adminDao = new AdminDao(configuration);
    }

    @Transactional
    @Override
    public Admin save(final Admin admin) {
        adminDao.ctx()
                .insertInto(ADMIN)
                .set(ADMIN.ID, admin.getId())
                .set(ADMIN.PASSWORD, admin.getPassword())
                .set(ADMIN.USER_ID, admin.getUserId())
                .set(ADMIN.ROLE, admin.getRole())
                .set(ADMIN.CREATED_AT, admin.getCreatedAt())
                .set(ADMIN.UPDATED_AT, admin.getUpdatedAt())
                .set(ADMIN.DELETED_AT, admin.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(ADMIN.PASSWORD, excluded(ADMIN.PASSWORD))
                .set(ADMIN.USER_ID, excluded(ADMIN.USER_ID))
                .set(ADMIN.ROLE, excluded(ADMIN.ROLE))
                .set(ADMIN.UPDATED_AT, excluded(ADMIN.UPDATED_AT))
                .set(ADMIN.DELETED_AT, excluded(ADMIN.DELETED_AT))
                .execute();
        return admin;
    }

    @Transactional(readOnly = true)
    @Override
    public Admin findById(final String id) {
        final var record = adminDao.ctx()
                .selectFrom(ADMIN)
                .where(ADMIN.ID.eq(id))
                .and(ADMIN.DELETED_AT.isNull())
                .fetchOne();

        if (record == null) {
            return null;
        }

        return new Admin(
                record.getId(),
                record.getPassword(),
                record.getUserId(),
                record.getRole(),
                record.getCreatedAt(),
                record.getUpdatedAt(),
                record.getDeletedAt()
        );
    }

    @Transactional
    @Override
    public void deleteById(final String id) {
        adminDao.ctx()
                .update(ADMIN)
                .set(ADMIN.DELETED_AT, java.time.LocalDateTime.now())
                .where(ADMIN.ID.eq(id))
                .execute();
    }
}
