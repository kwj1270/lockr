package com.official.lockr.global.outbox;

import com.official.lockr.global.ddd.IntegrationDomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link IntegrationDomainEvent}의 {@code eventType} 문자열을 구체 클래스로 매핑하는 레지스트리.
 *
 * <p>{@code OutboxProcessor}가 폴링한 envelope을 어느 record 클래스로 역직렬화할지 결정한다.
 * 등록 누락 시 첫 publish 시점에 {@link UnknownEventTypeException}이 발생하므로, 신규
 * {@code IntegrationDomainEvent} 도입 시 반드시 등록을 추가해야 한다.
 *
 * <h2>등록 컨벤션</h2>
 * <p>각 도메인은 {@code infrastructure/} 하위에 자체 {@code @Configuration} 클래스를 두고
 * 생성자에서 등록한다. 패턴 예시:
 * <pre>{@code
 * @Configuration
 * public class FeeOutboxEventTypeConfig {
 *     public FeeOutboxEventTypeConfig(OutboxEventTypeRegistry registry) {
 *         registry.register("com.official.lockr.club.fee.fee_record.unpaid_notified",
 *                           UnpaidFeeNotifiedEvent.class);
 *     }
 * }
 * }</pre>
 *
 * <p>{@code eventType} 문자열은 reverse-DNS 형식 권장: {@code com.official.lockr.{bc}.{subdomain}.{aggregate}.{action}}.
 *
 * <h2>시작 시 검증</h2>
 * <p>{@code ApplicationReadyEvent} 시점에 등록 개수를 로깅한다. registry가 비어 있으면
 * WARN 레벨로 경고 — 운영 환경의 로그 알람으로 빈 registry를 즉시 감지할 수 있다.
 *
 * <h2>향후 자동 등록 검토 (ADR Follow-up)</h2>
 * <p>이벤트 타입이 10+로 늘어 누락 위험이 커지면 {@code @EventType} 어노테이션 + classpath
 * 스캔 기반 자동 등록을 도입한다.
 */
@Component
public class OutboxEventTypeRegistry {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventTypeRegistry.class);

    private final ConcurrentHashMap<String, Class<? extends IntegrationDomainEvent>> registry = new ConcurrentHashMap<>();

    public void register(final String eventType, final Class<? extends IntegrationDomainEvent> clazz) {
        registry.put(eventType, clazz);
    }

    public Class<? extends IntegrationDomainEvent> resolve(final String eventType) {
        final Class<? extends IntegrationDomainEvent> clazz = registry.get(eventType);
        if (clazz == null) {
            throw new UnknownEventTypeException(eventType);
        }
        return clazz;
    }

    @EventListener(ApplicationReadyEvent.class)
    void logStartup() {
        if (registry.isEmpty()) {
            log.warn("OutboxEventTypeRegistry is empty — no IntegrationDomainEvent types registered. " +
                    "First publish will fail with UnknownEventTypeException. " +
                    "Register types in each domain's @Configuration constructor.");
        } else {
            log.info("OutboxEventTypeRegistry initialized: {} event types registered.", registry.size());
        }
    }
}
