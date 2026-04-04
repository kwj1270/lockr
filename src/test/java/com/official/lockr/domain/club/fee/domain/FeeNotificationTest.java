package com.official.lockr.domain.club.fee.domain;

import com.official.lockr.domain.club.fee.domain.event.UnpaidFeeNotifiedEvent;
import com.official.lockr.global.ddd.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FeeNotificationTest {

    @Nested
    @DisplayName("init() - 알림 이력 생성")
    class Init {

        @Test
        @DisplayName("init() 호출 시 id, clubId, year, month, sentBy, memberIds, createdAt이 설정되어야 한다")
        void shouldCreateWithRequiredFields() {
            List<String> memberIds = List.of("member-1", "member-2");

            FeeNotification notification = FeeNotification.init("club-1", 2025, 4, "sender-1", memberIds);

            assertThat(notification.getId()).isNotNull();
            assertThat(notification.getClubId()).isEqualTo("club-1");
            assertThat(notification.getYear()).isEqualTo(2025);
            assertThat(notification.getMonth()).isEqualTo(4);
            assertThat(notification.getSentBy()).isEqualTo("sender-1");
            assertThat(notification.getMemberIds()).containsExactly("member-1", "member-2");
            assertThat(notification.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("init() 호출 시 UnpaidFeeNotifiedEvent가 발행되어야 한다")
        void shouldPublishUnpaidFeeNotifiedEvent() {
            FeeNotification notification = FeeNotification.init("club-1", 2025, 4, "sender-1", List.of("member-1"));

            List<DomainEvent> events = collectEvents(notification);

            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(UnpaidFeeNotifiedEvent.class);
        }

        @Test
        @DisplayName("UnpaidFeeNotifiedEvent에 sentBy 필드가 포함되어야 한다")
        void shouldIncludeSentByInEvent() {
            FeeNotification notification = FeeNotification.init("club-1", 2025, 4, "sender-1", List.of("member-1", "member-2"));

            List<DomainEvent> events = collectEvents(notification);
            UnpaidFeeNotifiedEvent event = (UnpaidFeeNotifiedEvent) events.get(0);

            assertThat(event.clubId()).isEqualTo("club-1");
            assertThat(event.year()).isEqualTo(2025);
            assertThat(event.month()).isEqualTo(4);
            assertThat(event.sentBy()).isEqualTo("sender-1");
            assertThat(event.memberIds()).containsExactly("member-1", "member-2");
            assertThat(event.occurredAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class Identity {

        @Test
        @DisplayName("같은 id면 같은 객체여야 한다")
        void shouldBeEqualById() {
            FeeNotification notification = FeeNotification.init("club-1", 2025, 4, "sender-1", List.of("m-1"));

            assertThat(notification).isEqualTo(notification);
        }

        @Test
        @DisplayName("다른 id면 다른 객체여야 한다")
        void shouldNotBeEqualWithDifferentId() {
            FeeNotification n1 = FeeNotification.init("club-1", 2025, 4, "sender-1", List.of("m-1"));
            FeeNotification n2 = FeeNotification.init("club-1", 2025, 4, "sender-1", List.of("m-1"));

            assertThat(n1).isNotEqualTo(n2);
        }
    }

    private List<DomainEvent> collectEvents(final FeeNotification notification) {
        List<DomainEvent> collected = new ArrayList<>();
        notification.publish(collected::add);
        return collected;
    }
}
