package com.official.lockr.global.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.official.lockr.global.ddd.DomainEvent;
import com.official.lockr.global.ddd.IntegrationDomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxDomainEventPublisherTest {

    private InMemoryOutboxRepository outboxRepository;
    private List<Object> publishedEvents;
    private OutboxDomainEventPublisher publisher;

    @BeforeEach
    void setUp() {
        outboxRepository = new InMemoryOutboxRepository();
        publishedEvents = new ArrayList<>();
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        final CloudEventEnvelopeMapper envelopeMapper = new CloudEventEnvelopeMapper(objectMapper);
        publisher = new OutboxDomainEventPublisher(outboxRepository, publishedEvents::add, envelopeMapper);
    }

    @Test
    void shouldRouteToOutboxWhenIntegrationDomainEvent() {
        final IntegrationDomainEvent event = new StubIntegrationEvent("evt-001", "agg-001");

        publisher.publish(event);

        assertThat(outboxRepository.findAll()).hasSize(1);
        assertThat(outboxRepository.findAll().get(0).eventId()).isEqualTo("evt-001");
        assertThat(publishedEvents).isEmpty();
    }

    @Test
    void shouldRouteToSyncWhenNonIntegrationDomainEvent() {
        final DomainEvent event = new StubDomainEvent();

        publisher.publish(event);

        assertThat(outboxRepository.findAll()).isEmpty();
        assertThat(publishedEvents).hasSize(1);
        assertThat(publishedEvents.get(0)).isInstanceOf(StubDomainEvent.class);
    }

    private record StubIntegrationEvent(String eventId, String aggregateId) implements IntegrationDomainEvent {
        @Override
        public String eventType() {
            return "com.example.stub.event";
        }

        @Override
        public String source() {
            return "lockr://test";
        }

        @Override
        public LocalDateTime occurredAt() {
            return LocalDateTime.now();
        }
    }

    private static class StubDomainEvent implements DomainEvent {
    }
}
