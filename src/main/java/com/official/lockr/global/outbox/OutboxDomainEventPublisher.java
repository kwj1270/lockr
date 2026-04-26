package com.official.lockr.global.outbox;

import com.official.lockr.global.ddd.DomainEvent;
import com.official.lockr.global.ddd.DomainEventPublisher;
import com.official.lockr.global.ddd.IntegrationDomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class OutboxDomainEventPublisher implements DomainEventPublisher {

    private final OutboxRepository outboxRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CloudEventEnvelopeMapper envelopeMapper;

    public OutboxDomainEventPublisher(final OutboxRepository outboxRepository,
                                      final ApplicationEventPublisher applicationEventPublisher,
                                      final CloudEventEnvelopeMapper envelopeMapper) {
        this.outboxRepository = outboxRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.envelopeMapper = envelopeMapper;
    }

    @Override
    public void publish(final DomainEvent domainEvent) {
        if (domainEvent instanceof IntegrationDomainEvent event) {
            final CloudEventEnvelope envelope = envelopeMapper.toEnvelope(event);
            outboxRepository.insert(new OutboxEntry(envelope));
        } else {
            applicationEventPublisher.publishEvent(domainEvent);
        }
    }
}
