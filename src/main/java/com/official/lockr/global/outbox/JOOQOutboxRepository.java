package com.official.lockr.global.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.generated.tables.DomainEventOutboxJOOQEntity.DOMAIN_EVENT_OUTBOX;

@Repository
public class JOOQOutboxRepository implements OutboxRepository {

    private final DSLContext dsl;
    private final ObjectMapper objectMapper;

    public JOOQOutboxRepository(final Configuration configuration,
                                @Qualifier(OutboxJacksonConfig.OUTBOX_OBJECT_MAPPER) final ObjectMapper objectMapper) {
        this.dsl = configuration.dsl();
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void insert(final OutboxEntry entry) {
        try {
            final String payload = objectMapper.writeValueAsString(entry.envelope());
            dsl.insertInto(DOMAIN_EVENT_OUTBOX)
                    .set(DOMAIN_EVENT_OUTBOX.EVENT_ID, entry.eventId())
                    .set(DOMAIN_EVENT_OUTBOX.EVENT_TYPE, entry.eventType())
                    .set(DOMAIN_EVENT_OUTBOX.SOURCE, entry.source())
                    .set(DOMAIN_EVENT_OUTBOX.AGGREGATE_ID, entry.aggregateId())
                    .set(DOMAIN_EVENT_OUTBOX.OCCURRED_AT, entry.occurredAt())
                    .set(DOMAIN_EVENT_OUTBOX.PAYLOAD, JSON.json(payload))
                    .set(DOMAIN_EVENT_OUTBOX.CREATED_AT, LocalDateTime.now())
                    .execute();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Outbox entry serialization failed", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<OutboxEntry> findUnprocessed(final int limit) {
        return dsl.selectFrom(DOMAIN_EVENT_OUTBOX)
                .where(DOMAIN_EVENT_OUTBOX.PROCESSED.eq(false))
                .and(DOMAIN_EVENT_OUTBOX.RETRY_COUNT.lt(MAX_RETRY))
                .orderBy(DOMAIN_EVENT_OUTBOX.SEQ.asc())
                .limit(limit)
                .fetch(record -> {
                    try {
                        final CloudEventEnvelope envelope = objectMapper.readValue(
                                record.getPayload().data(), CloudEventEnvelope.class);
                        return new OutboxEntry(envelope);
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException("Outbox entry deserialization failed", e);
                    }
                });
    }

    @Transactional
    @Override
    public void markProcessed(final String eventId) {
        dsl.update(DOMAIN_EVENT_OUTBOX)
                .set(DOMAIN_EVENT_OUTBOX.PROCESSED, true)
                .set(DOMAIN_EVENT_OUTBOX.UPDATED_AT, LocalDateTime.now())
                .where(DOMAIN_EVENT_OUTBOX.EVENT_ID.eq(eventId))
                .execute();
    }

    @Transactional
    @Override
    public void incrementRetry(final String eventId) {
        dsl.update(DOMAIN_EVENT_OUTBOX)
                .set(DOMAIN_EVENT_OUTBOX.RETRY_COUNT, DOMAIN_EVENT_OUTBOX.RETRY_COUNT.add(1))
                .set(DOMAIN_EVENT_OUTBOX.UPDATED_AT, LocalDateTime.now())
                .where(DOMAIN_EVENT_OUTBOX.EVENT_ID.eq(eventId))
                .execute();
    }

    @Transactional(readOnly = true)
    @Override
    public long countStuck() {
        return dsl.fetchCount(
                dsl.selectFrom(DOMAIN_EVENT_OUTBOX)
                        .where(DOMAIN_EVENT_OUTBOX.PROCESSED.eq(false))
                        .and(DOMAIN_EVENT_OUTBOX.RETRY_COUNT.ge(MAX_RETRY))
        );
    }
}
