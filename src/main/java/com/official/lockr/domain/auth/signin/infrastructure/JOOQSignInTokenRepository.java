package com.official.lockr.domain.auth.signin.infrastructure;

import com.official.lockr.domain.auth.signin.domain.SignInToken;
import com.official.lockr.domain.auth.signin.domain.SignInTokenRepository;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.SignInTokensDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.generated.Tables.SIGN_IN_TOKENS;
import static org.jooq.impl.DSL.excluded;

@Repository
public class JOOQSignInTokenRepository implements SignInTokenRepository {

    private final SignInTokensDao signInTokensDao;

    public JOOQSignInTokenRepository(final Configuration configuration) {
        this.signInTokensDao = new SignInTokensDao(configuration);
    }

    @Transactional
    @Override
    public SignInToken save(final SignInToken token) {
        signInTokensDao.ctx()
                .insertInto(SIGN_IN_TOKENS)
                .set(SIGN_IN_TOKENS.ID, token.getId())
                .set(SIGN_IN_TOKENS.USER_ID, token.getUserId())
                .set(SIGN_IN_TOKENS.SIGN_IN_ID, token.getSignInId())
                .set(SIGN_IN_TOKENS.TOKEN, token.getToken())
                .set(SIGN_IN_TOKENS.EXPIRES_AT, token.getExpiresAt())
                .set(SIGN_IN_TOKENS.CREATED_AT, token.getCreatedAt())
                .set(SIGN_IN_TOKENS.DELETED_AT, token.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(SIGN_IN_TOKENS.DELETED_AT, excluded(SIGN_IN_TOKENS.DELETED_AT))
                .execute();
        return token;
    }

    @Transactional(readOnly = true)
    @Override
    public SignInToken findByToken(final String token) {
        final var record = signInTokensDao.ctx()
                .selectFrom(SIGN_IN_TOKENS)
                .where(SIGN_IN_TOKENS.TOKEN.eq(token))
                .and(SIGN_IN_TOKENS.DELETED_AT.isNull())
                .fetchOne();

        if (record == null) {
            return null;
        }

        return new SignInToken(
                record.getId(),
                record.getUserId(),
                record.getSignInId(),
                record.getToken(),
                record.getExpiresAt(),
                record.getCreatedAt(),
                record.getDeletedAt()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public SignInToken findById(final String id) {
        final var record = signInTokensDao.ctx()
                .selectFrom(SIGN_IN_TOKENS)
                .where(SIGN_IN_TOKENS.ID.eq(id))
                .and(SIGN_IN_TOKENS.DELETED_AT.isNull())
                .fetchOne();

        if (record == null) {
            return null;
        }

        return new SignInToken(
                record.getId(),
                record.getUserId(),
                record.getSignInId(),
                record.getToken(),
                record.getExpiresAt(),
                record.getCreatedAt(),
                record.getDeletedAt()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<SignInToken> findBySignInId(final String signInId) {
        return signInTokensDao.ctx()
                .selectFrom(SIGN_IN_TOKENS)
                .where(SIGN_IN_TOKENS.SIGN_IN_ID.eq(signInId))
                .and(SIGN_IN_TOKENS.DELETED_AT.isNull())
                .fetch()
                .stream()
                .map(record -> new SignInToken(
                        record.getId(),
                        record.getUserId(),
                        record.getSignInId(),
                        record.getToken(),
                        record.getExpiresAt(),
                        record.getCreatedAt(),
                        record.getDeletedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void deleteById(final String id) {
        signInTokensDao.ctx()
                .update(SIGN_IN_TOKENS)
                .set(SIGN_IN_TOKENS.DELETED_AT, java.time.LocalDateTime.now())
                .where(SIGN_IN_TOKENS.ID.eq(id))
                .execute();
    }
}
