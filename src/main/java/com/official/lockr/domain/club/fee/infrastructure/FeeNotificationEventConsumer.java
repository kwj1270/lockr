package com.official.lockr.domain.club.fee.infrastructure;

import com.official.lockr.domain.club.fee.domain.FeeClub;
import com.official.lockr.domain.club.fee.domain.event.UnpaidFeeNotifiedEvent;
import com.official.lockr.domain.notification.application.command.CreateFeeUnpaidNotificationCommand;
import com.official.lockr.domain.notification.application.usecase.CreateFeeUnpaidNotificationUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
public class FeeNotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(FeeNotificationEventConsumer.class);

    private final FeeClub feeClub;
    private final CreateFeeUnpaidNotificationUseCase createFeeUnpaidNotificationUseCase;

    public FeeNotificationEventConsumer(
            final FeeClub feeClub,
            final CreateFeeUnpaidNotificationUseCase createFeeUnpaidNotificationUseCase
    ) {
        this.feeClub = feeClub;
        this.createFeeUnpaidNotificationUseCase = createFeeUnpaidNotificationUseCase;
    }

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUnpaidFeeNotifiedEvent(final UnpaidFeeNotifiedEvent event) {
        try {
            final List<String> userIds = feeClub.findUserIdsByMemberIds(event.clubId(), event.memberIds());
            if (userIds.isEmpty()) {
                return;
            }
            createFeeUnpaidNotificationUseCase.create(new CreateFeeUnpaidNotificationCommand(
                    event.clubId(),
                    event.year(),
                    event.month(),
                    event.sentBy(),
                    userIds
            ));
        } catch (Exception e) {
            log.error("Failed to handle unpaid fee notified event for clubId={}, year={}, month={}: {}",
                    event.clubId(), event.year(), event.month(), e.getMessage(), e);
        }
    }
}
