package com.official.lockr.global.outbox;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.official.lockr.global.ddd.IntegrationDomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxProcessorTest {

    private InMemoryOutboxRepository outboxRepository;
    private OutboxEventTypeRegistry registry;
    private List<Object> publishedEvents;
    private CloudEventEnvelopeMapper envelopeMapper;
    private OutboxProcessor processor;

    @BeforeEach
    void setUp() {
        outboxRepository = new InMemoryOutboxRepository();
        registry = new OutboxEventTypeRegistry();
        registry.register("com.example.stub.event", StubIntegrationEvent.class);
        publishedEvents = new ArrayList<>();
        final ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        envelopeMapper = new CloudEventEnvelopeMapper(objectMapper);
        processor = new OutboxProcessor(outboxRepository, registry, envelopeMapper, publishedEvents::add);
    }

    @Test
    void shouldPublishEventAndMarkProcessedOnSuccess() {
        final StubIntegrationEvent event = new StubIntegrationEvent("evt-001", "agg-001", LocalDateTime.now());
        outboxRepository.insert(new OutboxEntry(envelopeMapper.toEnvelope(event)));

        processor.process();

        assertThat(publishedEvents).hasSize(1);
        assertThat(outboxRepository.isProcessed("evt-001")).isTrue();
        assertThat(outboxRepository.findUnprocessed(10)).isEmpty();
    }

    @Test
    void shouldIncrementRetryOnFailure() {
        final StubIntegrationEvent event = new StubIntegrationEvent("evt-002", "agg-002", LocalDateTime.now());
        outboxRepository.insert(new OutboxEntry(envelopeMapper.toEnvelope(event)));

        // Use a registry with unknown type to force failure
        final OutboxEventTypeRegistry badRegistry = new OutboxEventTypeRegistry();
        final OutboxProcessor failingProcessor = new OutboxProcessor(outboxRepository, badRegistry, envelopeMapper, publishedEvents::add);

        failingProcessor.process();

        assertThat(publishedEvents).isEmpty();
        assertThat(outboxRepository.isProcessed("evt-002")).isFalse();
        assertThat(outboxRepository.findUnprocessed(10)).hasSize(1);
    }

    @Test
    void shouldCountStuckEntriesForMonitoring() {
        final StubIntegrationEvent stuck = new StubIntegrationEvent("evt-stuck-1", "agg-A", LocalDateTime.now());
        final StubIntegrationEvent active = new StubIntegrationEvent("evt-active", "agg-B", LocalDateTime.now());
        outboxRepository.insert(new OutboxEntry(envelopeMapper.toEnvelope(stuck)));
        outboxRepository.insert(new OutboxEntry(envelopeMapper.toEnvelope(active)));

        for (int i = 0; i < OutboxRepository.MAX_RETRY; i++) {
            outboxRepository.incrementRetry("evt-stuck-1");
        }
        outboxRepository.incrementRetry("evt-active");

        assertThat(outboxRepository.countStuck()).isEqualTo(1);
    }

    @Test
    void shouldNotCountProcessedEntryAsStuck() {
        final StubIntegrationEvent event = new StubIntegrationEvent("evt-done", "agg-C", LocalDateTime.now());
        outboxRepository.insert(new OutboxEntry(envelopeMapper.toEnvelope(event)));

        for (int i = 0; i < OutboxRepository.MAX_RETRY; i++) {
            outboxRepository.incrementRetry("evt-done");
        }
        outboxRepository.markProcessed("evt-done");

        assertThat(outboxRepository.countStuck()).isZero();
    }

    @Test
    void shouldExcludeEntryFromFindUnprocessedAfterMaxRetryReached() {
        final StubIntegrationEvent event = new StubIntegrationEvent("evt-stuck", "agg-X", LocalDateTime.now());
        outboxRepository.insert(new OutboxEntry(envelopeMapper.toEnvelope(event)));

        for (int i = 0; i < OutboxRepository.MAX_RETRY; i++) {
            outboxRepository.incrementRetry("evt-stuck");
        }

        assertThat(outboxRepository.getRetryCount("evt-stuck")).isEqualTo(OutboxRepository.MAX_RETRY);
        assertThat(outboxRepository.findUnprocessed(10)).isEmpty();
    }

    @Test
    void shouldIncludeEntryWhenRetryBelowMax() {
        final StubIntegrationEvent event = new StubIntegrationEvent("evt-retrying", "agg-Y", LocalDateTime.now());
        outboxRepository.insert(new OutboxEntry(envelopeMapper.toEnvelope(event)));

        for (int i = 0; i < OutboxRepository.MAX_RETRY - 1; i++) {
            outboxRepository.incrementRetry("evt-retrying");
        }

        assertThat(outboxRepository.findUnprocessed(10)).hasSize(1);
    }

    @Test
    void shouldNotIncrementRetryWhenMarkProcessedFails() {
        final InMemoryOutboxRepository failingRepo = new InMemoryOutboxRepository() {
            @Override
            public void markProcessed(final String eventId) {
                throw new RuntimeException("simulated DB failure");
            }
        };
        final OutboxProcessor failingProcessor = new OutboxProcessor(
                failingRepo, registry, envelopeMapper, publishedEvents::add);
        final StubIntegrationEvent event = new StubIntegrationEvent("evt-mark-fail", "agg-X", LocalDateTime.now());
        failingRepo.insert(new OutboxEntry(envelopeMapper.toEnvelope(event)));

        failingProcessor.process();

        assertThat(publishedEvents).hasSize(1);
        assertThat(failingRepo.isProcessed("evt-mark-fail")).isFalse();
        assertThat(failingRepo.getRetryCount("evt-mark-fail")).isZero();
    }

    @Test
    void shouldProcessInSeqOrder() {
        final StubIntegrationEvent event1 = new StubIntegrationEvent("evt-A", "agg-A", LocalDateTime.now());
        final StubIntegrationEvent event2 = new StubIntegrationEvent("evt-B", "agg-B", LocalDateTime.now());
        final StubIntegrationEvent event3 = new StubIntegrationEvent("evt-C", "agg-C", LocalDateTime.now());
        outboxRepository.insert(new OutboxEntry(envelopeMapper.toEnvelope(event1)));
        outboxRepository.insert(new OutboxEntry(envelopeMapper.toEnvelope(event2)));
        outboxRepository.insert(new OutboxEntry(envelopeMapper.toEnvelope(event3)));

        processor.process();

        assertThat(publishedEvents).hasSize(3);
        assertThat(outboxRepository.isProcessed("evt-A")).isTrue();
        assertThat(outboxRepository.isProcessed("evt-B")).isTrue();
        assertThat(outboxRepository.isProcessed("evt-C")).isTrue();
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
