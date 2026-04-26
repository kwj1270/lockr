package com.official.lockr.global.inbox;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.jooq.generated.tables.DomainEventInboxJOOQEntity.DOMAIN_EVENT_INBOX;

@Repository
public class JOOQInboxRepository implements InboxRepository {

    private final DSLContext dsl;

    public JOOQInboxRepository(final Configuration configuration) {
        this.dsl = configuration.dsl();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public boolean insertIfAbsent(final String eventId, final String consumerName) {
        try {
            dsl.insertInto(DOMAIN_EVENT_INBOX)
                    .set(DOMAIN_EVENT_INBOX.EVENT_ID, eventId)
                    .set(DOMAIN_EVENT_INBOX.CONSUMER_NAME, consumerName)
                    .set(DOMAIN_EVENT_INBOX.PROCESSED_AT, LocalDateTime.now())
                    .execute();
            return true;
        } catch (DataAccessException e) {
            if (e.getCause() instanceof java.sql.SQLIntegrityConstraintViolationException) {
                return false;
            }
            throw e;
        }
    }
}
