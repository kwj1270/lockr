package com.official.lockr.global.outbox;

import com.official.lockr.global.ddd.IntegrationDomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Outbox 폴링 프로세서. {@code @Scheduled(fixedDelay=100ms)}로 미처리 entry를 publish.
 *
 * <h2>단일 노드 가정</h2>
 * <p>본 구현은 <b>단일 노드 운영</b>을 가정한다. {@code findUnprocessed}에 row-level lock
 * 또는 분산 락이 없으므로 멀티 노드 배포 시 동일 entry를 여러 노드가 동시에 읽어 중복
 * publish가 발생할 수 있다. 컨슈머가 {@code IdempotentEventHandler}로 보호되어 있다면
 * 결과적 안전성은 유지되나, retry_count가 양쪽에서 따로 증가하여 stuck 임계 도달이
 * 빨라지는 등의 부작용이 있다.
 *
 * <h2>멀티 노드 전환 시점에 도입 검토</h2>
 * <ul>
 *   <li>{@code ShedLock} (또는 동등 분산 락) — {@code @SchedulerLock}으로 process()를
 *       leader 노드에서만 실행. 코드 변경 최소.</li>
 *   <li>{@code SELECT ... FOR UPDATE SKIP LOCKED} + claim 컬럼 — row 단위 분산 처리.
 *       처리량이 단일 노드 한계에 도달했을 때 검토.</li>
 * </ul>
 */
@Component
public class OutboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(OutboxProcessor.class);
    private static final int BATCH_SIZE = 100;

    private final OutboxRepository outboxRepository;
    private final OutboxEventTypeRegistry registry;
    private final CloudEventEnvelopeMapper envelopeMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    public OutboxProcessor(final OutboxRepository outboxRepository,
                           final OutboxEventTypeRegistry registry,
                           final CloudEventEnvelopeMapper envelopeMapper,
                           final ApplicationEventPublisher applicationEventPublisher) {
        this.outboxRepository = outboxRepository;
        this.registry = registry;
        this.envelopeMapper = envelopeMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Stuck entry 모니터링. 5분 주기로 retry 상한 도달 entry 수를 확인하고
     * 0보다 크면 ERROR 로그를 발행한다. 운영 환경의 로그 알람/Micrometer
     * gauge로 흡수해 즉시 감지·대응한다.
     */
    @Scheduled(fixedDelay = 300_000)
    public void monitorStuck() {
        final long stuck = outboxRepository.countStuck();
        if (stuck > 0) {
            log.error("Outbox stuck entries detected: count={} (retry_count >= {}). Manual investigation required.", stuck, OutboxRepository.MAX_RETRY);
        }
    }

    @Scheduled(fixedDelay = 100)
    public void process() {
        final List<OutboxEntry> entries = outboxRepository.findUnprocessed(BATCH_SIZE);
        for (final OutboxEntry entry : entries) {
            if (!publish(entry)) {
                continue;
            }
            markProcessed(entry);
        }
    }

    private boolean publish(final OutboxEntry entry) {
        try {
            final Class<? extends IntegrationDomainEvent> clazz = registry.resolve(entry.eventType());
            final IntegrationDomainEvent event = envelopeMapper.toEvent(entry.envelope(), clazz);
            applicationEventPublisher.publishEvent(event);
            return true;
        } catch (UnknownEventTypeException e) {
            log.error("Unknown event type in outbox: eventId={}, eventType={}. Verify registry registration.", entry.eventId(), entry.eventType());
            outboxRepository.incrementRetry(entry.eventId());
            return false;
        } catch (Exception e) {
            log.warn("Failed to publish outbox entry: eventId={}", entry.eventId(), e);
            outboxRepository.incrementRetry(entry.eventId());
            return false;
        }
    }

    private void markProcessed(final OutboxEntry entry) {
        try {
            outboxRepository.markProcessed(entry.eventId());
        } catch (Exception e) {
            // 의도적으로 incrementRetry를 호출하지 않는다.
            // publish는 이미 성공했으므로 다음 polling에서 재publish되며,
            // 컨슈머는 inbox 기반 멱등성으로 중복 처리를 방어한다.
            log.error("markProcessed failed after successful publish: eventId={}. " +
                    "Event will be re-published; consumers must guard idempotency via inbox.",
                    entry.eventId(), e);
        }
    }
}
