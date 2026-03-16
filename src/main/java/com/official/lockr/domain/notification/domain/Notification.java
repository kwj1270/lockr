package com.official.lockr.domain.notification.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.official.lockr.domain.notification.domain.event.CreatedNotificationEvent;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.official.lockr.global.util.UlidUtils.generateUlid;

public class Notification extends AggregateRoot {
    private final String id;
    private final String userId;
    private final String clubId;
    private final NotificationType type;
    private final String title;
    private final String content;
    private final JsonNode data;
    private boolean isRead;
    private final String actionType;
    private final String actionUrl;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private Notification(
            final String id,
            final String userId,
            final String clubId,
            final NotificationType type,
            final String title,
            final String content,
            final JsonNode data,
            final boolean isRead,
            final String actionType,
            final String actionUrl,
            final LocalDateTime createdAt,
            final LocalDateTime updatedAt,
            final LocalDateTime deletedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.clubId = clubId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.data = data;
        this.isRead = isRead;
        this.actionType = actionType;
        this.actionUrl = actionUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static Notification init(
            final String userId,
            final String clubId,
            final NotificationType type,
            final String title,
            final String content,
            final JsonNode data,
            final String actionType,
            final String actionUrl
    ) {
        final String id = generateUlid();
        final LocalDateTime now = LocalDateTime.now();
        final Notification notification = new Notification(
                id,
                userId,
                clubId,
                type,
                title,
                content,
                data,
                false,
                actionType,
                actionUrl,
                now,
                now,
                null
        );
        notification.addEvent(new CreatedNotificationEvent(
                id,
                userId,
                clubId,
                type.name(),
                title,
                now
        ));
        return notification;
    }

    public static Notification from(
            final String id,
            final String userId,
            final String clubId,
            final NotificationType type,
            final String title,
            final String content,
            final JsonNode data,
            final boolean isRead,
            final String actionType,
            final String actionUrl,
            final LocalDateTime createdAt,
            final LocalDateTime updatedAt,
            final LocalDateTime deletedAt
    ) {
        return new Notification(
                id,
                userId,
                clubId,
                type,
                title,
                content,
                data,
                isRead,
                actionType,
                actionUrl,
                createdAt,
                updatedAt,
                deletedAt
        );
    }

    public static Notification create(
            final String id,
            final String userId,
            final String clubId,
            final NotificationType type,
            final String title,
            final String content,
            final JsonNode data,
            final String actionType,
            final String actionUrl
    ) {
        final LocalDateTime now = LocalDateTime.now();
        final Notification notification = new Notification(
                id,
                userId,
                clubId,
                type,
                title,
                content,
                data,
                false,
                actionType,
                actionUrl,
                now,
                now,
                null
        );
        notification.addEvent(new CreatedNotificationEvent(
                id,
                userId,
                clubId,
                type.name(),
                title,
                now
        ));
        return notification;
    }

    public void markAsRead() {
        this.isRead = true;
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getClubId() {
        return clubId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public JsonNode getData() {
        return data;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getActionType() {
        return actionType;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Notification that = (Notification) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
