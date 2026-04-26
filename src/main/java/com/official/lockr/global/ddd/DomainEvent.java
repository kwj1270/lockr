package com.official.lockr.global.ddd;

/**
 * 도메인 이벤트 마커 인터페이스.
 *
 * <p>같은 JVM/BC 안에서 sync로 처리되는 in-process 이벤트의 베이스. Spring
 * {@code ApplicationEventPublisher}를 통해 즉시 발행되며 별도 영속화 없음.
 * 발행자 트랜잭션 안에서 리스너가 실행되므로 {@code @TransactionalEventListener}
 * 사용 시 phase에 주의.
 *
 * <p>BC 경계를 넘거나 at-least-once + idempotency 보장이 필요한 경우는
 * {@link IntegrationDomainEvent}를 사용한다.
 *
 * <p>구현 컨벤션: Java {@code record}, 과거형 명사 (e.g. {@code FoundClubEvent}).
 */
public interface DomainEvent {
}
