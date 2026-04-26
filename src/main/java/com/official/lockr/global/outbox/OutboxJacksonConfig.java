package com.official.lockr.global.outbox;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Outbox 전용 ObjectMapper.
 *
 * <p>전역 ObjectMapper와 격리한다. envelope 메타데이터를 data에 재주입할 때 record component에
 * 없는 필드(예: hard-coded eventType/source)가 입력에 포함될 수 있으므로
 * {@code FAIL_ON_UNKNOWN_PROPERTIES = false}가 필요하다. 이 설정을 전역 ObjectMapper에 두면
 * REST 컨트롤러나 외부 응답의 strict 검증까지 깨므로 outbox 경계에서만 적용한다.
 */
@Configuration
public class OutboxJacksonConfig {

    public static final String OUTBOX_OBJECT_MAPPER = "outboxObjectMapper";

    @Bean(OUTBOX_OBJECT_MAPPER)
    public ObjectMapper outboxObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
