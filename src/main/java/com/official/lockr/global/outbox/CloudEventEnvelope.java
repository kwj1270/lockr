package com.official.lockr.global.outbox;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * CloudEvents 1.0 스펙의 envelope 데이터 표현.
 *
 * <p>순수 데이터 record. {@code IntegrationDomainEvent}와의 변환은
 * {@link CloudEventEnvelopeMapper}에 위임한다.
 */
public record CloudEventEnvelope(
        String specversion,
        String id,
        String source,
        String type,
        String subject,
        LocalDateTime time,
        String datacontenttype,
        Map<String, Object> data
) {
}
