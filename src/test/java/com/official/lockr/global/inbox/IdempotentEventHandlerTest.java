package com.official.lockr.global.inbox;

import com.official.lockr.global.ddd.IntegrationDomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdempotentEventHandlerTest {

    private InMemoryInboxRepository inboxRepository;
    private List<TestIntegrationEvent> handledEvents;
    private TestIdempotentEventHandler handler;

    @BeforeEach
    void setUp() {
        inboxRepository = new InMemoryInboxRepository();
        handledEvents = new ArrayList<>();
        handler = new TestIdempotentEventHandler(inboxRepository, handledEvents);
    }

    @Test
    void shouldHandleFirstEventAndInsertInbox() {
        final TestIntegrationEvent event = new TestIntegrationEvent("evt-001", "agg-001");

        handler.handle(event);

        assertThat(handledEvents).hasSize(1);
        assertThat(handledEvents.get(0).eventId()).isEqualTo("evt-001");
    }

    @Test
    void shouldSkipDuplicateEvent() {
        final TestIntegrationEvent event = new TestIntegrationEvent("evt-001", "agg-001");

        handler.handle(event);
        handler.handle(event);

        assertThat(handledEvents).hasSize(1);
    }

    @Test
    void shouldPropagateDoHandleExceptionSoTransactionCanRollback() {
        final TestIntegrationEvent event = new TestIntegrationEvent("evt-001", "agg-001");
        handler.throwOnDoHandle = true;

        assertThatThrownBy(() -> handler.handle(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("doHandle failed");
    }

    private static class TestIdempotentEventHandler extends IdempotentEventHandler<TestIntegrationEvent> {

        private final List<TestIntegrationEvent> handledEvents;
        boolean throwOnDoHandle = false;

        public TestIdempotentEventHandler(final InboxRepository inbox,
                                           final List<TestIntegrationEvent> handledEvents) {
            super(inbox);
            this.handledEvents = handledEvents;
        }

        @Override
        protected String consumerName() {
            return "test.consumer";
        }

        @Override
        protected void doHandle(final TestIntegrationEvent event) {
            if (throwOnDoHandle) {
                throw new RuntimeException("doHandle failed");
            }
            handledEvents.add(event);
        }
    }

    private record TestIntegrationEvent(
            String eventId,
            String aggregateId
    ) implements IntegrationDomainEvent {
        @Override
        public String eventType() {
            return "com.example.test.event";
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
}
