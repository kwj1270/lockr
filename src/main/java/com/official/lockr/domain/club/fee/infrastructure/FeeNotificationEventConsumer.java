package com.official.lockr.domain.club.fee.infrastructure;

import com.official.lockr.domain.club.fee.domain.FeeClub;
import com.official.lockr.domain.club.fee.domain.event.UnpaidFeeNotifiedEvent;
import com.official.lockr.domain.notification.application.command.CreateFeeUnpaidNotificationCommand;
import com.official.lockr.domain.notification.application.usecase.CreateFeeUnpaidNotificationUseCase;
import com.official.lockr.global.inbox.IdempotentEventHandler;
import com.official.lockr.global.inbox.InboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FeeNotificationEventConsumer extends IdempotentEventHandler<UnpaidFeeNotifiedEvent> {

    private static final Logger log = LoggerFactory.getLogger(FeeNotificationEventConsumer.class);

    private final FeeClub feeClub;
    private final CreateFeeUnpaidNotificationUseCase createFeeUnpaidNotificationUseCase;

    public FeeNotificationEventConsumer(
            final InboxRepository inbox,
            final FeeClub feeClub,
            final CreateFeeUnpaidNotificationUseCase createFeeUnpaidNotificationUseCase
    ) {
        super(inbox);
        this.feeClub = feeClub;
        this.createFeeUnpaidNotificationUseCase = createFeeUnpaidNotificationUseCase;
    }

    @Override
    protected String consumerName() {
        return "fee.unpaid_notification";
    }

    @Override
    protected void doHandle(final UnpaidFeeNotifiedEvent event) {
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
    }

    @EventListener
    public void onEvent(final UnpaidFeeNotifiedEvent event) {
        handle(event);
    }
}
