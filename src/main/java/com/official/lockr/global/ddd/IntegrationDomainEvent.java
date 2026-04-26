package com.official.lockr.global.ddd;

import java.time.LocalDateTime;

/**
 * Outbox 경로로 발행되는 도메인 이벤트 마커.
 *
 * <p>{@link DomainEvent}와 달리 영속 저장(domain_event_outbox) 후 폴러가 비동기 릴레이한다.
 * 최대 ~100ms 지연이 발생하며 at-least-once로 전달되므로 구독자는 반드시
 * {@code IdempotentEventHandler<E>}를 상속하거나 별도의 멱등성 방어를 갖춰야 한다.
 *
 * <h2>{@code DomainEvent} vs {@code IntegrationDomainEvent} 선택 기준</h2>
 * <table>
 *   <tr><th>상황</th><th>선택</th></tr>
 *   <tr>
 *     <td>같은 BC 내 in-process 리스너 (예: AR 변경 → projection 업데이트)</td>
 *     <td>{@code DomainEvent}</td>
 *   </tr>
 *   <tr>
 *     <td>BC 경계를 넘는 이벤트 (예: club → notification)</td>
 *     <td>{@code IntegrationDomainEvent}</td>
 *   </tr>
 *   <tr>
 *     <td>외부 시스템(Kafka/webhook/SQS)으로 흘릴 가능성이 있음</td>
 *     <td>{@code IntegrationDomainEvent}</td>
 *   </tr>
 *   <tr>
 *     <td>at-least-once + idempotency 보장 필요</td>
 *     <td>{@code IntegrationDomainEvent}</td>
 *   </tr>
 *   <tr>
 *     <td>sync 응답에 이벤트 결과가 즉시 반영돼야 함 (~100ms 지연 허용 불가)</td>
 *     <td>{@code DomainEvent}</td>
 *   </tr>
 *   <tr>
 *     <td>리스너 실패 시 재시도가 필요 없고 손실 가능</td>
 *     <td>{@code DomainEvent}</td>
 *   </tr>
 * </table>
 *
 * <h2>구현 시 주의</h2>
 * <ul>
 *   <li>{@code eventId}는 AR의 {@code addEvent()} 시점에 ULID로 생성. 재시도 시에도
 *       동일 객체가 재사용되어야 outbox UNIQUE 제약이 중복 차단 역할을 한다.</li>
 *   <li>{@code eventType}은 reverse-DNS 형식 권장:
 *       {@code com.official.lockr.{bc}.{subdomain}.{aggregate}.{action}}.</li>
 *   <li>{@code source}는 URI 형식 권장: {@code lockr://{bc}/{subdomain}}.</li>
 *   <li>각 도메인은 {@code OutboxEventTypeRegistry}에 자신의 이벤트 타입을 등록해야 한다
 *       (보통 {@code @Configuration} 클래스 생성자에서).</li>
 * </ul>
 */
public interface IntegrationDomainEvent extends DomainEvent {
    String eventId();
    String eventType();
    String source();
    String aggregateId();
    LocalDateTime occurredAt();
}
