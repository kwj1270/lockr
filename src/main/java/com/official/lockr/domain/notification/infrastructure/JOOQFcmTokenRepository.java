package com.official.lockr.domain.notification.infrastructure;

import com.official.lockr.domain.notification.domain.FcmToken;
import com.official.lockr.domain.notification.domain.FcmTokenRepository;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.FcmTokensDao;
import org.jooq.generated.tables.pojos.FcmTokensEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.jooq.generated.tables.FcmTokensJOOQEntity.FCM_TOKENS;
import static org.jooq.impl.DSL.excluded;

@Repository
public class JOOQFcmTokenRepository implements FcmTokenRepository {

    private final FcmTokensDao fcmTokensDao;

    public JOOQFcmTokenRepository(final Configuration configuration) {
        this.fcmTokensDao = new FcmTokensDao(configuration);
    }

    @Transactional
    @Override
    public FcmToken save(final FcmToken fcmToken) {
        fcmTokensDao.ctx()
                .insertInto(FCM_TOKENS)
                .set(FCM_TOKENS.ID, fcmToken.getId())
                .set(FCM_TOKENS.USER_ID, fcmToken.getUserId())
                .set(FCM_TOKENS.TOKEN, fcmToken.getToken())
                .set(FCM_TOKENS.DEVICE_ID, fcmToken.getDeviceId())
                .set(FCM_TOKENS.CREATED_AT, fcmToken.getCreatedAt())
                .set(FCM_TOKENS.UPDATED_AT, fcmToken.getUpdatedAt())
                .onDuplicateKeyUpdate()
                .set(FCM_TOKENS.TOKEN, excluded(FCM_TOKENS.TOKEN))
                .set(FCM_TOKENS.UPDATED_AT, excluded(FCM_TOKENS.UPDATED_AT))
                .execute();
        return fcmToken;
    }

    @Override
    public List<FcmToken> findByUserId(final String userId) {
        return fcmTokensDao.ctx()
                .selectFrom(FCM_TOKENS)
                .where(FCM_TOKENS.USER_ID.eq(userId))
                .fetchInto(FcmTokensEntity.class)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<String> findTokensByUserIds(final List<String> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return fcmTokensDao.ctx()
                .select(FCM_TOKENS.TOKEN)
                .from(FCM_TOKENS)
                .where(FCM_TOKENS.USER_ID.in(userIds))
                .fetchInto(String.class);
    }

    @Transactional
    @Override
    public void deleteByUserId(final String userId) {
        fcmTokensDao.ctx()
                .deleteFrom(FCM_TOKENS)
                .where(FCM_TOKENS.USER_ID.eq(userId))
                .execute();
    }

    @Transactional
    @Override
    public void deleteByToken(final String token) {
        fcmTokensDao.ctx()
                .deleteFrom(FCM_TOKENS)
                .where(FCM_TOKENS.TOKEN.eq(token))
                .execute();
    }

    private FcmToken toDomain(final FcmTokensEntity entity) {
        return FcmToken.from(
                entity.getId(),
                entity.getUserId(),
                entity.getToken(),
                entity.getDeviceId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
