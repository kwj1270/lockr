package com.official.lockr.global.inbox;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis SETNX + 보상 패턴 기반 멱등 실행 구현체. (학습 참조용)
 *
 * <p>{@link IdempotentExecutor}의 도구 다양성을 보여주는 구현 예시. 운영 환경에서
 * Bean으로 자동 등록되지 않도록 {@code @Profile("redis-inbox")}로 격리한다.
 * 일반 환경에서는 {@link TransactionalRdbIdempotentExecutor}가 활성 상태로 유지된다.
 *
 * <h2>동작</h2>
 * <ol>
 *   <li>SET NX로 멱등 키 획득 (TTL 7일 — long-tail redelivery 보호)</li>
 *   <li>{@code doHandle.run()} 실행</li>
 *   <li>예외 시 키 명시 삭제 (보상) → 동일 이벤트 재처리 가능</li>
 * </ol>
 *
 * <h2>RDB 트랜잭션과의 차이 — 진짜 ACID 아님</h2>
 * <ul>
 *   <li>doHandle 안의 RDB {@code Repository.save}는 자체 {@code @Transactional}로 commit/rollback,
 *       Redis 키는 별개 시스템이라 분리 처리됨. 두 시스템의 원자성은 보장되지 않는다.</li>
 *   <li>보상({@code redis.delete}) 자체가 실패하면 키가 stuck. TTL 만료(7일)로 자연 회복.</li>
 *   <li>at-least-once + 보상 + TTL 조합으로 <em>best-effort</em> 멱등성. 진짜 once 아님.</li>
 * </ul>
 *
 * <h2>운영 도입 검토 사항</h2>
 * <ul>
 *   <li>doHandle이 RDB와 외부 시스템(FCM 등) 모두 호출하는 mixed 케이스의 트랜잭션 의미 재검토</li>
 *   <li>TTL 적정 값 — 너무 짧으면 늦은 redelivery에 멱등 깨짐, 너무 길면 키 누적</li>
 *   <li>Saga 보상 / Outbox-claim 패턴 등 다른 도구 비교</li>
 *   <li>ADR-0009 "트레이드오프" + ADR-0006(Outbox/Inbox 인프라) 함께 참조</li>
 * </ul>
 */
@Component
@Profile("redis-inbox")
public class CompensatingRedisIdempotentExecutor implements IdempotentExecutor {

    private static final Duration TTL = Duration.ofDays(7);

    private final StringRedisTemplate redis;

    public CompensatingRedisIdempotentExecutor(final StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void executeIfAbsent(
            final String eventId,
            final String consumerName,
            final Runnable doHandle
    ) {
        final String key = "inbox:" + consumerName + ":" + eventId;
        final Boolean acquired = redis.opsForValue().setIfAbsent(key, "1", TTL);
        if (!Boolean.TRUE.equals(acquired)) {
            return;
        }
        try {
            doHandle.run();
        } catch (RuntimeException e) {
            redis.delete(key);  // 보상: SETNX 롤백 → 재처리 가능
            throw e;
        }
    }
}
