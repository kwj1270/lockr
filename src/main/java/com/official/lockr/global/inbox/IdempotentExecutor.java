package com.official.lockr.global.inbox;

/**
 * 멱등 실행 추상 계약.
 *
 * <p>같은 {@code (eventId, consumerName)} 쌍으로 두 번 호출되어도 {@code doHandle}은
 * 최초 1회만 실행된다.
 *
 * <p>{@code doHandle}이 예외를 던지면 가드도 무효화되어 동일 이벤트를 재처리할 수 있다.
 * 원자성 보장 방식(트랜잭션 롤백, 수동 제거 등)은 구현체 책임이다.
 *
 * <p>구현체는 RDB 트랜잭션({@code TransactionalRdbIdempotentExecutor}), Redis Lua 스크립트,
 * NoOp(테스트 환경) 등 도구를 자유롭게 선택한다. 호출자(베이스 핸들러, Consumer)는 도구를 알 필요가 없다.
 */
public interface IdempotentExecutor {

    /**
     * {@code doHandle}을 멱등하게 실행한다.
     *
     * @param eventId      이벤트 고유 식별자
     * @param consumerName 컨슈머 식별 이름 (같은 이벤트를 여러 컨슈머가 독립 처리하므로 쌍으로 가드)
     * @param doHandle     실제 비즈니스 처리 로직
     */
    void executeIfAbsent(String eventId, String consumerName, Runnable doHandle);
}
