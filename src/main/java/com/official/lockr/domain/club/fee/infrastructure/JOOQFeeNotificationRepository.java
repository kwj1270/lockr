package com.official.lockr.domain.club.fee.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.official.lockr.domain.club.fee.domain.FeeNotification;
import com.official.lockr.domain.club.fee.domain.FeeNotificationRepository;
import com.official.lockr.global.ddd.DomainEventPublisher;
import org.jooq.Configuration;
import org.jooq.JSON;
import org.jooq.generated.tables.daos.FeeNotificationsDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.jooq.generated.tables.FeeNotificationsJOOQEntity.FEE_NOTIFICATIONS;

@Repository
public class JOOQFeeNotificationRepository implements FeeNotificationRepository {

    private final FeeNotificationsDao feeNotificationsDao;
    private final DomainEventPublisher domainEventPublisher;
    private final ObjectMapper objectMapper;

    public JOOQFeeNotificationRepository(final Configuration configuration,
                                         final DomainEventPublisher domainEventPublisher,
                                         final ObjectMapper objectMapper) {
        this.feeNotificationsDao = new FeeNotificationsDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public FeeNotification save(final FeeNotification notification) {
        feeNotificationsDao.ctx()
                .insertInto(FEE_NOTIFICATIONS)
                .set(FEE_NOTIFICATIONS.ID, notification.getId())
                .set(FEE_NOTIFICATIONS.CLUB_ID, notification.getClubId())
                .set(FEE_NOTIFICATIONS.YEAR, notification.getYear())
                .set(FEE_NOTIFICATIONS.MONTH, notification.getMonth())
                .set(FEE_NOTIFICATIONS.SENT_BY, notification.getSentBy())
                .set(FEE_NOTIFICATIONS.MEMBER_IDS, JSON.json(toJson(notification.getMemberIds())))
                .set(FEE_NOTIFICATIONS.CREATED_AT, notification.getCreatedAt())
                .execute();
        notification.publish(domainEventPublisher);
        return notification;
    }

    @Override
    public int countByClubIdAndYearAndMonth(final String clubId, final int year, final int month) {
        final Integer count = feeNotificationsDao.ctx()
                .selectCount()
                .from(FEE_NOTIFICATIONS)
                .where(FEE_NOTIFICATIONS.CLUB_ID.eq(clubId))
                .and(FEE_NOTIFICATIONS.YEAR.eq(year))
                .and(FEE_NOTIFICATIONS.MONTH.eq(month))
                .fetchOne(0, Integer.class);
        return count != null ? count : 0;
    }

    private String toJson(final List<String> memberIds) {
        try {
            return objectMapper.writeValueAsString(memberIds);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("member_ids JSON 직렬화 실패", e);
        }
    }
}
