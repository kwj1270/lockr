package com.official.lockr.global.outbox;

import com.official.lockr.global.ddd.IntegrationDomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboxEventTypeRegistryTest {

    private OutboxEventTypeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new OutboxEventTypeRegistry();
        registry.register("com.example.stub.event", StubIntegrationEvent.class);
    }

    @Test
    void shouldResolveRegisteredEventType() {
        assertThat(registry.resolve("com.example.stub.event"))
                .isEqualTo(StubIntegrationEvent.class);
    }

    @Test
    void shouldThrowUnknownEventTypeException() {
        assertThatThrownBy(() -> registry.resolve("com.example.unknown.event"))
                .isInstanceOf(UnknownEventTypeException.class)
                .hasMessageContaining("com.example.unknown.event");
    }

    public record StubIntegrationEvent(
            String eventId,
            String aggregateId,
            LocalDateTime occurredAt
    ) implements IntegrationDomainEvent {
        @Override
        public String eventType() {
            return "com.example.stub.event";
        }

        @Override
        public String source() {
            return "lockr://test";
        }
    }
}
