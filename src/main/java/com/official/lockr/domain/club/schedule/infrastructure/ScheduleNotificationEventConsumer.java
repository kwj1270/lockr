package com.official.lockr.domain.club.schedule.infrastructure;

import com.official.lockr.domain.club.schedule.domain.ScheduleClub;
import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.official.lockr.domain.club.schedule.domain.event.CancelledScheduleEvent;
import com.official.lockr.domain.club.schedule.domain.event.CreatedScheduleEvent;
import com.official.lockr.domain.club.schedule.domain.event.UpdatedScheduleEvent;
import com.official.lockr.domain.club.schedule.domain.vo.MatchDetailData;
import com.official.lockr.domain.notification.application.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.isNull;

@Component
public class ScheduleNotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ScheduleNotificationEventConsumer.class);

    private final ScheduleClub scheduleClub;
    private final NotificationService notificationService;

    public ScheduleNotificationEventConsumer(
            final ScheduleClub scheduleClub,
            final NotificationService notificationService
    ) {
        this.scheduleClub = scheduleClub;
        this.notificationService = notificationService;
    }

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCreatedScheduleEvent(final CreatedScheduleEvent event) {
        try {
            if (event.scheduleType() != ScheduleType.MATCH) {
                return;
            }
            if (!(event.detail() instanceof MatchDetailData matchDetail)) {
                return;
            }
            sendNotificationToOpponentClub(event, matchDetail);
        } catch (Exception e) {
            log.error("Failed to handle created schedule event for scheduleId={}, clubId={}: {}",
                    event.scheduleId(), event.clubId(), e.getMessage(), e);
        }
    }

    private void sendNotificationToOpponentClub(
            final CreatedScheduleEvent event,
            final MatchDetailData matchDetail
    ) {
        final String myClubId = event.clubId();
        final String opponentClubId = myClubId.equals(matchDetail.homeClubId())
                ? matchDetail.awayClubId()
                : matchDetail.homeClubId();

        if (isNull(opponentClubId) || opponentClubId.isEmpty()) {
            return;
        }

        if (!scheduleClub.existsClub(opponentClubId)) {
            return;
        }

        final String myClubName = scheduleClub.findClubNameById(myClubId);
        if (isNull(myClubName)) {
            return;
        }

        notificationService.createScheduleLinkNotification(
                opponentClubId,
                event.scheduleId(),
                myClubId,
                myClubName,
                event.title(),
                event.scheduleTime().toString()
        );
    }

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUpdatedScheduleEvent(final UpdatedScheduleEvent event) {
        try {
            log.info("Schedule updated: scheduleId={}, clubId={}, newTime={}",
                    event.scheduleId(), event.clubId(), event.newScheduleTime());
        } catch (Exception e) {
            log.error("Failed to handle updated schedule event for scheduleId={}, clubId={}: {}",
                    event.scheduleId(), event.clubId(), e.getMessage(), e);
        }
    }

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCancelledScheduleEvent(final CancelledScheduleEvent event) {
        try {
            log.info("Schedule cancelled: scheduleId={}, clubId={}, type={}",
                    event.scheduleId(), event.clubId(), event.scheduleType());
        } catch (Exception e) {
            log.error("Failed to handle cancelled schedule event for scheduleId={}, clubId={}: {}",
                    event.scheduleId(), event.clubId(), e.getMessage(), e);
        }
    }
}
