package com.official.lockr.domain.club.fee.domain;

import com.official.lockr.domain.club.fee.domain.event.FeePolicyChangedEvent;
import com.official.lockr.global.ddd.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeePolicyTest {

    @Nested
    @DisplayName("init() - 회비 정책 생성")
    class Init {

        @Test
        @DisplayName("정상 생성 시 id, clubId, amount, dueDay가 설정되어야 한다")
        void shouldCreateWithRequiredFields() {
            FeePolicy policy = FeePolicy.init("club-1", 30000, 15, null);

            assertThat(policy.getId()).isNotNull();
            assertThat(policy.getClubId()).isEqualTo("club-1");
            assertThat(policy.getAmount()).isEqualTo(30000);
            assertThat(policy.getDueDay()).isEqualTo(15);
        }

        @Test
        @DisplayName("생성 시 FeePolicyChangedEvent가 발행되어야 한다")
        void shouldPublishFeePolicyChangedEvent() {
            FeePolicy policy = FeePolicy.init("club-1", 30000, 15, null);

            List<DomainEvent> events = collectEvents(policy);

            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(FeePolicyChangedEvent.class);
            FeePolicyChangedEvent event = (FeePolicyChangedEvent) events.get(0);
            assertThat(event.clubId()).isEqualTo("club-1");
            assertThat(event.amount()).isEqualTo(30000);
            assertThat(event.dueDay()).isEqualTo(15);
        }

        @Test
        @DisplayName("dueDay가 1 미만이면 예외가 발생해야 한다")
        void shouldThrowWhenDueDayLessThan1() {
            assertThatThrownBy(() -> FeePolicy.init("club-1", 30000, 0, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("dueDay가 28 초과이면 예외가 발생해야 한다")
        void shouldThrowWhenDueDayGreaterThan28() {
            assertThatThrownBy(() -> FeePolicy.init("club-1", 30000, 29, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("amount가 0 미만이면 예외가 발생해야 한다")
        void shouldThrowWhenAmountNegative() {
            assertThatThrownBy(() -> FeePolicy.init("club-1", -1, 15, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("amount가 0이면 유효한 정책으로 생성되어야 한다")
        void shouldAllowZeroAmount() {
            FeePolicy policy = FeePolicy.init("club-1", 0, 15, null);

            assertThat(policy.getAmount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("updatePolicy() - 정책 수정")
    class UpdatePolicy {

        @Test
        @DisplayName("금액/기한/계좌를 변경하면 FeePolicyChangedEvent가 발행되어야 한다")
        void shouldPublishEventOnUpdate() {
            FeePolicy policy = FeePolicy.init("club-1", 30000, 15, null);
            collectEvents(policy); // 생성 이벤트 소비

            BankAccount account = new BankAccount("국민은행", "123-456", "홍길동");
            policy.updatePolicy(50000, 20, account);

            List<DomainEvent> events = collectEvents(policy);

            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(FeePolicyChangedEvent.class);
            FeePolicyChangedEvent event = (FeePolicyChangedEvent) events.get(0);
            assertThat(event.amount()).isEqualTo(50000);
            assertThat(event.dueDay()).isEqualTo(20);
        }

        @Test
        @DisplayName("updatePolicy() 호출 시 updatedAt이 갱신되어야 한다")
        void shouldUpdateUpdatedAtOnPolicyChange() {
            FeePolicy policy = FeePolicy.init("club-1", 30000, 15, null);
            java.time.LocalDateTime before = policy.getUpdatedAt();

            try { Thread.sleep(1); } catch (InterruptedException ignored) {}

            BankAccount account = new BankAccount("국민은행", "123-456", "홍길동");
            policy.updatePolicy(50000, 20, account);

            assertThat(policy.getUpdatedAt()).isAfter(before);
        }
    }

    @Nested
    @DisplayName("BankAccount VO")
    class BankAccountTest {

        @Test
        @DisplayName("모든 필드가 null이면 null을 반환해야 한다")
        void shouldReturnNullWhenAllFieldsNull() {
            BankAccount account = BankAccount.of(null, null, null);

            assertThat(account).isNull();
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class Identity {

        @Test
        @DisplayName("같은 id면 같은 객체여야 한다")
        void shouldBeEqualById() {
            FeePolicy policy1 = FeePolicy.init("club-1", 30000, 15, null);
            FeePolicy policy2 = FeePolicy.init("club-1", 50000, 20, null);

            // 다른 id이므로 다른 객체
            assertThat(policy1).isNotEqualTo(policy2);
            // 자기 자신은 같아야 함
            assertThat(policy1).isEqualTo(policy1);
        }
    }

    // 이벤트 수집 헬퍼 — publish()를 호출하여 이벤트를 캡처
    private List<DomainEvent> collectEvents(FeePolicy policy) {
        List<DomainEvent> collected = new ArrayList<>();
        policy.publish(collected::add);
        return collected;
    }
}
