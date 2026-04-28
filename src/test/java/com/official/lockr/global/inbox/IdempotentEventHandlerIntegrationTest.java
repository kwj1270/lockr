package com.official.lockr.global.inbox;

import com.official.lockr.global.ddd.IntegrationDomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Spring 컨텍스트에서 publishEvent → onApplicationEvent → @Transactional AOP 경로 검증.
 *
 * <p>self-invocation 회귀 방지: 베이스의 onApplicationEvent()가 Spring AOP proxy를 통해
 * 올바르게 진입점이 됨을 확인한다.
 *
 * <p>최소 Spring 컨텍스트(AOP + 이벤트 발행만)를 구성하여 Firebase/DB/Flyway 의존성 없이 실행한다.
 * InMemoryInboxRepository를 사용하므로 트랜잭션 롤백 시나리오는 예외 전파 여부로 검증한다.
 */
@SpringBootTest(classes = {
        IdempotentEventHandlerIntegrationTest.TestInfraConfig.class
})
class IdempotentEventHandlerIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TestInboxRepositoryHolder inboxHolder;

    @BeforeEach
    void setUp() {
        inboxHolder.inboxRepository().clear();
        inboxHolder.consumer().reset();
    }

    @Test
    @DisplayName("publishEvent 호출 시 onApplicationEvent가 Spring AOP proxy를 통해 실행되고 doHandle이 1회 호출된다")
    void shouldDispatchViaApplicationListenerOnPublishEvent() {
        final TestIntegrationDomainEvent event = new TestIntegrationDomainEvent("integ-evt-001", "agg-001");

        eventPublisher.publishEvent(event);

        assertThat(inboxHolder.consumer().handledEventIds()).hasSize(1);
        assertThat(inboxHolder.consumer().handledEventIds().get(0)).isEqualTo("integ-evt-001");
    }

    @Test
    @DisplayName("동일 eventId로 두 번 publishEvent해도 doHandle은 1회만 실행된다 (inbox 멱등성)")
    void shouldBeIdempotentOnDuplicatePublish() {
        final TestIntegrationDomainEvent event = new TestIntegrationDomainEvent("integ-evt-002", "agg-002");

        eventPublisher.publishEvent(event);
        eventPublisher.publishEvent(event);

        assertThat(inboxHolder.consumer().handledEventIds()).hasSize(1);
    }

    @Test
    @DisplayName("doHandle에서 예외 발생 시 예외가 onApplicationEvent 밖으로 전파된다 (트랜잭션 롤백 트리거)")
    void shouldPropagateExceptionFromDoHandleSoTransactionRollsBack() {
        inboxHolder.consumer().failOnNext = true;
        final TestIntegrationDomainEvent event = new TestIntegrationDomainEvent("integ-evt-003", "agg-003");

        assertThatThrownBy(() -> eventPublisher.publishEvent(event))
                .isInstanceOf(RuntimeException.class)
                .satisfies(ex -> {
                    // 예외가 직접 전파되거나 Spring이 래핑할 수 있으므로 root cause를 확인한다
                    Throwable root = ex;
                    while (root.getCause() != null) {
                        root = root.getCause();
                    }
                    assertThat(root.getMessage()).isEqualTo("simulated doHandle failure");
                });

        assertThat(inboxHolder.consumer().handledEventIds()).isEmpty();
    }

    // -----------------------------------------------------------------------
    // Test infrastructure
    // -----------------------------------------------------------------------

    record TestIntegrationDomainEvent(
            String eventId,
            String aggregateId
    ) implements IntegrationDomainEvent {
        @Override
        public String eventType() {
            return "com.official.lockr.test.integration_domain_event";
        }

        @Override
        public String source() {
            return "lockr://test/inbox";
        }

        @Override
        public LocalDateTime occurredAt() {
            return LocalDateTime.now();
        }
    }

    @Configuration
    @EnableTransactionManagement
    static class TestInfraConfig {

        @Bean
        TestInboxRepositoryHolder testInboxRepositoryHolder() {
            final InMemoryInboxRepository inbox = new InMemoryInboxRepository();
            final TestEventConsumer consumer = new TestEventConsumer(inbox);
            return new TestInboxRepositoryHolder(inbox, consumer);
        }

        @Bean
        InboxRepository inboxRepository(final TestInboxRepositoryHolder holder) {
            return holder.inboxRepository();
        }

        @Bean
        TestEventConsumer testEventConsumer(final TestInboxRepositoryHolder holder) {
            return holder.consumer();
        }

        @Bean
        PlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(final Object transaction, final TransactionDefinition definition) {
                }

                @Override
                protected void doCommit(final DefaultTransactionStatus status) {
                }

                @Override
                protected void doRollback(final DefaultTransactionStatus status) {
                }
            };
        }
    }

    static class TestInboxRepositoryHolder {
        private final InMemoryInboxRepository inboxRepository;
        private final TestEventConsumer consumer;

        TestInboxRepositoryHolder(final InMemoryInboxRepository inboxRepository,
                                  final TestEventConsumer consumer) {
            this.inboxRepository = inboxRepository;
            this.consumer = consumer;
        }

        InMemoryInboxRepository inboxRepository() {
            return inboxRepository;
        }

        TestEventConsumer consumer() {
            return consumer;
        }
    }

    static class TestEventConsumer extends IdempotentEventHandler<TestIntegrationDomainEvent> {

        private final List<String> handledEventIds = new ArrayList<>();
        volatile boolean failOnNext = false;

        TestEventConsumer(final InboxRepository inbox) {
            super(inbox);
        }

        @Override
        protected String consumerName() {
            return "test.integration_consumer";
        }

        @Override
        protected void doHandle(final TestIntegrationDomainEvent event) {
            if (failOnNext) {
                throw new RuntimeException("simulated doHandle failure");
            }
            handledEventIds.add(event.eventId());
        }

        List<String> handledEventIds() {
            return List.copyOf(handledEventIds);
        }

        void reset() {
            handledEventIds.clear();
            failOnNext = false;
        }
    }
}
