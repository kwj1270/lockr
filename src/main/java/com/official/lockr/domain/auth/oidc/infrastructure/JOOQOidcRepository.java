package com.official.lockr.domain.auth.oidc.infrastructure;

import com.official.lockr.domain.auth.oidc.domain.vo.ProviderType;
import com.official.lockr.domain.auth.oidc.domain.Oidc;
import com.official.lockr.domain.auth.oidc.domain.OidcRepository;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.OidcDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.jooq.generated.Tables.OIDC;
import static org.jooq.impl.DSL.excluded;

@Repository
public class JOOQOidcRepository implements OidcRepository {

    private final OidcDao userOidcDao;

    public JOOQOidcRepository(final Configuration configuration) {
        this.userOidcDao = new OidcDao(configuration);
    }

    @Transactional
    @Override
    public Oidc save(final Oidc oidc) {
        upsertOidc(oidc);
        return oidc;
    }

    @Override
    public Oidc find(final String identifier, final String provider) {
        final var record = userOidcDao.ctx()
                .selectFrom(OIDC)
                .where(OIDC.IDENTIFIER.eq(identifier))
                .and(OIDC.PROVIDER.eq(provider))
                .and(OIDC.DELETED_AT.isNull())
                .fetchOne();

        if (record == null) {
            return null;
        }

        return new Oidc(
                record.getId(),
                record.getUserId(),
                ProviderType.valueOf(record.getProvider()),
                record.getIdentifier(),
                record.getMetadata(),
                record.getCreatedAt(),
                record.getUpdatedAt(),
                record.getDeletedAt()
        );
    }

    @Transactional
    @Override
    public void deleteByUserId(final String userId) {
        userOidcDao.ctx()
                .update(OIDC)
                .set(OIDC.DELETED_AT, LocalDateTime.now())
                .where(OIDC.USER_ID.eq(userId))
                .and(OIDC.DELETED_AT.isNull())
                .execute();
    }

    private void upsertOidc(final Oidc oidc) {
        userOidcDao.ctx()
                .insertInto(OIDC)
                .set(OIDC.ID, oidc.getId())
                .set(OIDC.USER_ID, oidc.getUserId())
                .set(OIDC.PROVIDER, Objects.nonNull(oidc.getProvider()) ? oidc.getProvider().name() : null)
                .set(OIDC.IDENTIFIER, oidc.getIdentifier())
                .set(OIDC.METADATA, oidc.getMetadata())
                .set(OIDC.CREATED_AT, oidc.getCreatedAt())
                .set(OIDC.UPDATED_AT, oidc.getUpdatedAt())
                .set(OIDC.DELETED_AT, oidc.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(OIDC.USER_ID, excluded(OIDC.USER_ID))
                .set(OIDC.METADATA, excluded(OIDC.METADATA))
                .set(OIDC.UPDATED_AT, excluded(OIDC.UPDATED_AT))
                .set(OIDC.DELETED_AT, excluded(OIDC.DELETED_AT))
                .execute();
    }
}
