package com.official.lockr.domain.notification.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.official.lockr.domain.notification.domain.Notification;
import com.official.lockr.domain.notification.domain.NotificationRepository;
import com.official.lockr.domain.notification.domain.NotificationType;
import com.official.lockr.global.ddd.DomainEventPublisher;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.JSON;
import org.jooq.generated.tables.daos.NotificationsDao;
import org.jooq.generated.tables.pojos.NotificationsEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Objects.isNull;
import static org.jooq.generated.tables.NotificationsJOOQEntity.NOTIFICATIONS;
import static org.jooq.impl.DSL.excluded;

@ConditionalOnMissingBean(InMemoryNotificationRepository.class)
@Repository
public class JOOQNotificationRepository implements NotificationRepository {

    private final NotificationsDao notificationsDao;
    private final ObjectMapper objectMapper;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQNotificationRepository(
            final Configuration configuration,
            final ObjectMapper objectMapper,
            final DomainEventPublisher domainEventPublisher
    ) {
        this.notificationsDao = new NotificationsDao(configuration);
        this.objectMapper = objectMapper;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Transactional
    @Override
    public Notification save(final Notification notification) {
        upsertNotification(notification);
        notification.publish(domainEventPublisher);
        return notification;
    }

    private void upsertNotification(final Notification notification) {
        notificationsDao.ctx()
                .insertInto(NOTIFICATIONS)
                .set(NOTIFICATIONS.ID, notification.getId())
                .set(NOTIFICATIONS.USER_ID, notification.getUserId())
                .set(NOTIFICATIONS.CLUB_ID, notification.getClubId())
                .set(NOTIFICATIONS.TYPE, notification.getType().name())
                .set(NOTIFICATIONS.TITLE, notification.getTitle())
                .set(NOTIFICATIONS.CONTENT, notification.getContent())
                .set(NOTIFICATIONS.DATA, serializeData(notification.getData()))
                .set(NOTIFICATIONS.IS_READ, notification.isRead())
                .set(NOTIFICATIONS.ACTION_TYPE, notification.getActionType())
                .set(NOTIFICATIONS.ACTION_URL, notification.getActionUrl())
                .set(NOTIFICATIONS.CREATED_AT, notification.getCreatedAt())
                .set(NOTIFICATIONS.UPDATED_AT, notification.getUpdatedAt())
                .set(NOTIFICATIONS.DELETED_AT, notification.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(NOTIFICATIONS.IS_READ, excluded(NOTIFICATIONS.IS_READ))
                .set(NOTIFICATIONS.UPDATED_AT, excluded(NOTIFICATIONS.UPDATED_AT))
                .set(NOTIFICATIONS.DELETED_AT, excluded(NOTIFICATIONS.DELETED_AT))
                .execute();
    }

    @Nullable
    @Override
    public Notification findById(final String id) {
        final NotificationsEntity entity = notificationsDao.ctx()
                .selectFrom(NOTIFICATIONS)
                .where(NOTIFICATIONS.ID.eq(id))
                .fetchOneInto(NotificationsEntity.class);
        if (isNull(entity)) {
            return null;
        }
        return toDomain(entity);
    }

    @Override
    public List<Notification> findByUserId(final String userId) {
        return notificationsDao.ctx()
                .selectFrom(NOTIFICATIONS)
                .where(NOTIFICATIONS.USER_ID.eq(userId))
                .and(NOTIFICATIONS.DELETED_AT.isNull())
                .orderBy(NOTIFICATIONS.CREATED_AT.desc())
                .fetchInto(NotificationsEntity.class)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Notification> findByUserIdAndClubId(final String userId, final String clubId) {
        return notificationsDao.ctx()
                .selectFrom(NOTIFICATIONS)
                .where(NOTIFICATIONS.USER_ID.eq(userId))
                .and(NOTIFICATIONS.CLUB_ID.eq(clubId))
                .and(NOTIFICATIONS.DELETED_AT.isNull())
                .orderBy(NOTIFICATIONS.CREATED_AT.desc())
                .fetchInto(NotificationsEntity.class)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Notification toDomain(final NotificationsEntity entity) {
        return Notification.from(
                entity.getId(),
                entity.getUserId(),
                entity.getClubId(),
                NotificationType.valueOf(entity.getType()),
                entity.getTitle(),
                entity.getContent(),
                deserializeData(entity.getData()),
                entity.getIsRead(),
                entity.getActionType(),
                entity.getActionUrl(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    @Nullable
    private JSON serializeData(final JsonNode data) {
        if (isNull(data)) {
            return null;
        }
        try {
            return JSON.json(objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize notification data", e);
        }
    }

    @Nullable
    private JsonNode deserializeData(final JSON data) {
        if (isNull(data)) {
            return null;
        }
        try {
            return objectMapper.readTree(data.data());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize notification data", e);
        }
    }
}
