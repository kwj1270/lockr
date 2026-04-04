package com.official.lockr.domain.club.fee.domain;

import com.official.lockr.domain.club.fee.domain.event.FeeRecordMarkedPaidEvent;
import com.official.lockr.domain.club.fee.domain.event.FeePolicyChangedEvent;
import com.official.lockr.domain.club.fee.domain.event.UnpaidFeeNotifiedEvent;
import com.official.lockr.global.ddd.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FeeRecordTest {

    @Nested
    @DisplayName("create() - 회비 납부 기록 생성")
    class Create {

        @Test
        @DisplayName("정상 생성 시 id, clubId, memberId, year, month가 설정되어야 한다")
        void shouldCreateWithRequiredFields() {
            FeeRecord record = FeeRecord.create("club-1", "member-1", 2025, 4);

            assertThat(record.getId()).isNotNull();
            assertThat(record.getClubId()).isEqualTo("club-1");
            assertThat(record.getMemberId()).isEqualTo("member-1");
            assertThat(record.getYear()).isEqualTo(2025);
            assertThat(record.getMonth()).isEqualTo(4);
        }

        @Test
        @DisplayName("생성 시 status는 UNPAID여야 한다")
        void shouldHaveUnpaidStatusOnCreate() {
            FeeRecord record = FeeRecord.create("club-1", "member-1", 2025, 4);

            assertThat(record.getStatus()).isEqualTo(FeeStatus.UNPAID);
        }

        @Test
        @DisplayName("생성 시 이벤트가 발행되지 않아야 한다")
        void shouldNotPublishEventOnCreate() {
            FeeRecord record = FeeRecord.create("club-1", "member-1", 2025, 4);

            List<DomainEvent> events = collectEvents(record);

            assertThat(events).isEmpty();
        }
    }

    @Nested
    @DisplayName("markPaid() - 납부 처리")
    class MarkPaid {

        @Test
        @DisplayName("markPaid() 호출 시 status가 PAID로 변경되어야 한다")
        void shouldChangStatusToPaid() {
            FeeRecord record = FeeRecord.create("club-1", "member-1", 2025, 4);

            record.markPaid("admin-1");

            assertThat(record.getStatus()).isEqualTo(FeeStatus.PAID);
        }

        @Test
        @DisplayName("markPaid() 호출 시 updatedBy가 기록되어야 한다")
        void shouldRecordUpdatedBy() {
            FeeRecord record = FeeRecord.create("club-1", "member-1", 2025, 4);

            record.markPaid("admin-1");

            assertThat(record.getUpdatedBy()).isEqualTo("admin-1");
        }

        @Test
        @DisplayName("markPaid() 호출 시 FeeRecordMarkedPaidEvent가 발행되어야 한다")
        void shouldPublishFeeRecordMarkedPaidEvent() {
            FeeRecord record = FeeRecord.create("club-1", "member-1", 2025, 4);

            record.markPaid("admin-1");

            List<DomainEvent> events = collectEvents(record);
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(FeeRecordMarkedPaidEvent.class);
            FeeRecordMarkedPaidEvent event = (FeeRecordMarkedPaidEvent) events.get(0);
            assertThat(event.recordId()).isEqualTo(record.getId());
            assertThat(event.clubId()).isEqualTo("club-1");
            assertThat(event.memberId()).isEqualTo("member-1");
            assertThat(event.year()).isEqualTo(2025);
            assertThat(event.month()).isEqualTo(4);
        }

        @Test
        @DisplayName("이미 PAID 상태에서 markPaid() 재호출 시 중복 이벤트가 발행되지 않아야 한다")
        void shouldNotPublishDuplicateEventWhenAlreadyPaid() {
            FeeRecord record = FeeRecord.create("club-1", "member-1", 2025, 4);
            record.markPaid("admin-1");
            collectEvents(record); // 첫 번째 이벤트 소비

            record.markPaid("admin-2");

            List<DomainEvent> events = collectEvents(record);
            assertThat(events).isEmpty();
        }
    }

    @Nested
    @DisplayName("markUnpaid() - 미납 처리")
    class MarkUnpaid {

        @Test
        @DisplayName("markUnpaid() 호출 시 status가 UNPAID로 변경되어야 한다")
        void shouldChangeStatusToUnpaid() {
            FeeRecord record = FeeRecord.create("club-1", "member-1", 2025, 4);
            record.markPaid("admin-1");
            collectEvents(record);

            record.markUnpaid();

            assertThat(record.getStatus()).isEqualTo(FeeStatus.UNPAID);
        }
    }

    @Nested
    @DisplayName("updateMemo() - 메모 설정")
    class UpdateMemo {

        @Test
        @DisplayName("updateMemo() 호출 시 memo가 설정되어야 한다")
        void shouldSetMemo() {
            FeeRecord record = FeeRecord.create("club-1", "member-1", 2025, 4);

            record.updateMemo("4월 회비 납부 완료");

            assertThat(record.getMemo()).isEqualTo("4월 회비 납부 완료");
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class Identity {

        @Test
        @DisplayName("같은 id면 같은 객체여야 한다")
        void shouldBeEqualById() {
            FeeRecord record = FeeRecord.create("club-1", "member-1", 2025, 4);

            assertThat(record).isEqualTo(record);
        }

        @Test
        @DisplayName("다른 id면 다른 객체여야 한다")
        void shouldNotBeEqualWithDifferentId() {
            FeeRecord record1 = FeeRecord.create("club-1", "member-1", 2025, 4);
            FeeRecord record2 = FeeRecord.create("club-1", "member-1", 2025, 4);

            assertThat(record1).isNotEqualTo(record2);
        }
    }

    @Nested
    @DisplayName("FeePolicyChangedEvent - 필드 검증")
    class FeePolicyChangedEventFields {

        @Test
        @DisplayName("FeePolicyChangedEvent는 policyId, clubId, amount, dueDay, changedAt 필드를 가져야 한다")
        void shouldHaveRequiredFields() {
            FeePolicy policy = FeePolicy.init("club-1", 30000, 15, null);
            List<DomainEvent> events = collectPolicyEvents(policy);

            assertThat(events).hasSize(1);
            FeePolicyChangedEvent event = (FeePolicyChangedEvent) events.get(0);
            assertThat(event.policyId()).isNotNull();
            assertThat(event.clubId()).isEqualTo("club-1");
            assertThat(event.amount()).isEqualTo(30000);
            assertThat(event.dueDay()).isEqualTo(15);
            assertThat(event.changedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("UnpaidFeeNotifiedEvent - 필드 검증")
    class UnpaidFeeNotifiedEventFields {

        @Test
        @DisplayName("UnpaidFeeNotifiedEvent는 clubId, year, month, notifiedMemberIds 필드를 가져야 한다")
        void shouldHaveRequiredFields() {
            List<String> memberIds = List.of("member-1", "member-2");

            UnpaidFeeNotifiedEvent event = new UnpaidFeeNotifiedEvent("club-1", 2025, 4, memberIds);

            assertThat(event.clubId()).isEqualTo("club-1");
            assertThat(event.year()).isEqualTo(2025);
            assertThat(event.month()).isEqualTo(4);
            assertThat(event.notifiedMemberIds()).containsExactly("member-1", "member-2");
        }
    }

    private List<DomainEvent> collectEvents(FeeRecord record) {
        List<DomainEvent> collected = new ArrayList<>();
        record.publish(collected::add);
        return collected;
    }

    private List<DomainEvent> collectPolicyEvents(FeePolicy policy) {
        List<DomainEvent> collected = new ArrayList<>();
        policy.publish(collected::add);
        return collected;
    }
}
