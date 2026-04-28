# ADR-0009: IdempotentExecutor — 추상-도구 분리 (도구 격리)

- **상태**: 승인됨 (학습)
- **날짜**: 2026-04-29
- **관련 레이어**: global/inbox

## 컨텍스트

ADR-0008에서 `IdempotentEventHandler` 베이스가 `ApplicationListener` 진입점을 흡수하여 self-invocation 함정을 해소했다. 그런데 베이스가 `@Transactional`과 `InboxRepository`를 직접 보유하면서 새로운 문제가 생겼다.

**"멱등성 보장"이라는 추상적 계약**과 **"RDB 트랜잭션"이라는 구체적 도구**가 같은 클래스에 혼재한다.

이 변경은 production 긴급도보다 **헥사고날 아키텍처의 "도구-추상 분리" 원칙을 몸으로 체험**하는 학습이 핵심 가치다.

### 체험 학습 포인트

- `@Transactional`은 Spring + RDB의 도구 결정임을 코드로 표현하는 방법
- 베이스(추상)가 도구를 모르면 미래에 Redis Lua, NoOp, Saga 보상 등 다른 도구로 교체할 때 베이스를 건드리지 않아도 됨을 확인
- 변경 전후로 인터페이스 경계가 어디에 있는지 ADR-0009로 결정 원칙 보존

## 고려한 선택지

### 옵션 A: 현행 유지 (기각)

`IdempotentEventHandler` 베이스가 `@Transactional` + `InboxRepository`를 직접 보유.

**기각 근거:**
- 도구 결정이 추상 레이어에 누출. 베이스를 변경하지 않고는 멱등 전략 교체 불가.
- 새 Consumer가 다른 멱등 전략(NoOp, Redis)을 원해도 베이스 수정 또는 상속 우회 필요.

### 옵션 B: IdempotentExecutor 인터페이스 추출 (채택)

`IdempotentExecutor` 인터페이스를 도입하여 `@Transactional` + `InboxRepository`를
`TransactionalRdbIdempotentExecutor`로 격리한다.

**장점:**
- 베이스는 추상(`IdempotentExecutor`)만 알고 도구는 모름
- Consumer는 `IdempotentExecutor` 구현체를 생성자 주입으로 교체하는 것만으로 멱등 전략 변경 가능
- 테스트에서 `InMemoryIdempotentExecutor`로 DB 없이 검증 가능

**단점:**
- production 코드 2파일 추가 (학습 목적에서는 오히려 장점)

## 결정

**옵션 B를 채택한다.**

`IdempotentExecutor` 인터페이스를 추출하고, RDB + 트랜잭션 구현은 `TransactionalRdbIdempotentExecutor`로 격리한다.

```java
// 추상 계약 — 도구 모름
public interface IdempotentExecutor {
    void executeIfAbsent(String eventId, String consumerName, Runnable doHandle);
}

// RDB 구현체 — 도구 격리
@Component
public class TransactionalRdbIdempotentExecutor implements IdempotentExecutor {

    private final InboxRepository inbox;

    @Transactional
    @Override
    public void executeIfAbsent(String eventId, String consumerName, Runnable doHandle) {
        if (!inbox.insertIfAbsent(eventId, consumerName)) return;
        doHandle.run();
    }
}

// 베이스 — @Transactional 0개, InboxRepository import 0개
public abstract class IdempotentEventHandler<E extends IntegrationDomainEvent>
        implements ApplicationListener<PayloadApplicationEvent<E>> {

    private final IdempotentExecutor executor;

    @Override
    public void onApplicationEvent(PayloadApplicationEvent<E> wrapper) {
        E event = wrapper.getPayload();
        executor.executeIfAbsent(event.eventId(), consumerName(), () -> doHandle(event));
    }
}
```

### 미래 구현체 — 학습 참조용

**`CompensatingRedisIdempotentExecutor`** — Redis SETNX + 보상 패턴 (**실제 파일로 보존**, `@Profile("redis-inbox")` 격리):

위치: `src/main/java/com/official/lockr/global/inbox/CompensatingRedisIdempotentExecutor.java`

운영 환경 자동 활성화 안 됨. `redis-inbox` 프로파일 활성 시에만 Bean 등록되어 학습 검증용으로 사용 가능. RDB 트랜잭션과의 차이(진짜 ACID 아님, 보상 + TTL 기반 best-effort 멱등성)는 클래스 Javadoc에 상세 기록.

```java
// CompensatingRedisIdempotentExecutor.java
public class CompensatingRedisIdempotentExecutor implements IdempotentExecutor {
    private final RedisTemplate<String, String> redis;

    @Override
    public void executeIfAbsent(String eventId, String consumerName, Runnable doHandle) {
        String key = "inbox:" + consumerName + ":" + eventId;
        Boolean acquired = redis.opsForValue().setIfAbsent(key, "1", Duration.ofDays(7));
        if (!Boolean.TRUE.equals(acquired)) return;
        try {
            doHandle.run();
        } catch (RuntimeException e) {
            redis.delete(key);  // 보상: SETNX 롤백
            throw e;
        }
    }
}
```

**`NoOpIdempotentExecutor`** — 도메인 자체가 멱등하거나 테스트/개발 환경:

```java
// 가상 예시 — 실제 파일 아님
public class NoOpIdempotentExecutor implements IdempotentExecutor {
    @Override
    public void executeIfAbsent(String eventId, String consumerName, Runnable doHandle) {
        doHandle.run();  // 가드 없이 항상 실행
    }
}
```

## 근거

- `@Transactional`은 Spring + RDB의 도구 결정. 베이스(추상)가 이를 몰라야 헥사고날 원칙에 부합.
- `JOOQInboxRepository.insertIfAbsent`는 `Propagation.MANDATORY`. `TransactionalRdbIdempotentExecutor`의 `@Transactional`이 외부 트랜잭션을 시작하여 기존 ADR-0008의 트랜잭션 경계를 보존.
- 통합 테스트에서 `TransactionalRdbIdempotentExecutor → InMemoryInboxRepository` 위임 경로를 검증하여 도구 격리 효과를 실제로 확인.

## 결과

- `IdempotentEventHandler` 클래스 파일에 `@Transactional` 0개, `InboxRepository` import 0개.
- `TransactionalRdbIdempotentExecutor`가 `@Transactional` + `InboxRepository` 두 가지 모두 보유.
- Consumer(`FeeNotificationEventConsumer`)는 생성자에서 `IdempotentExecutor`만 주입받음.
- 도구 결정이 `TransactionalRdbIdempotentExecutor` 한 군데에 격리됨.
