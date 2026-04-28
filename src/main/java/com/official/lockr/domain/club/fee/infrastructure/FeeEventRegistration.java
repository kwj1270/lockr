package com.official.lockr.domain.club.fee.infrastructure;

import com.official.lockr.domain.club.fee.domain.event.UnpaidFeeNotifiedEvent;
import com.official.lockr.global.outbox.OutboxEventTypeRegistry;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeeEventRegistration {

    public FeeEventRegistration(final OutboxEventTypeRegistry registry) {
        registry.register("com.official.lockr.club.fee.unpaid_notified", UnpaidFeeNotifiedEvent.class);
    }
}
