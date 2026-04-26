package com.official.lockr.global.outbox;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.official.lockr.global.ddd.IntegrationDomainEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@link IntegrationDomainEvent}와 {@link CloudEventEnvelope} 간 변환 책임을 가진다.
 *
 * <p>{@code CloudEventEnvelope}을 순수 데이터 record로 유지하기 위해 변환 로직과
 * Jackson 의존성을 본 클래스로 격리한다. envelope 메타데이터 필드(eventId/eventType/
 * source/aggregateId/occurredAt)는 직렬화 시 data 페이로드에서 제외하고, 역직렬화 시
 * 재주입한다.
 */
@Component
public class CloudEventEnvelopeMapper {

    // IntegrationDomainEvent의 envelope 메타데이터 메서드와 1:1 동기화. 인터페이스에 envelope-level 필드 추가/제거 시 함께 갱신할 것.
    private static final Set<String> ENVELOPE_FIELDS = Set.of(
            "eventId", "eventType", "source", "aggregateId", "occurredAt"
    );

    private final ObjectMapper objectMapper;

    public CloudEventEnvelopeMapper(
            @Qualifier(OutboxJacksonConfig.OUTBOX_OBJECT_MAPPER) final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CloudEventEnvelope toEnvelope(final IntegrationDomainEvent event) {
        final Map<String, Object> payload = new LinkedHashMap<>(
                objectMapper.convertValue(event, new TypeReference<Map<String, Object>>() {})
        );
        payload.keySet().removeAll(ENVELOPE_FIELDS);
        return new CloudEventEnvelope(
                "1.0",
                event.eventId(),
                event.source(),
                event.eventType(),
                event.aggregateId(),
                event.occurredAt(),
                "application/json",
                payload
        );
    }

    public <E extends IntegrationDomainEvent> E toEvent(final CloudEventEnvelope envelope, final Class<E> clazz) {
        final Map<String, Object> merged = new LinkedHashMap<>(envelope.data());
        merged.put("eventId", envelope.id());
        merged.put("eventType", envelope.type());
        merged.put("source", envelope.source());
        merged.put("aggregateId", envelope.subject());
        merged.put("occurredAt", envelope.time());
        return objectMapper.convertValue(merged, clazz);
    }
}
