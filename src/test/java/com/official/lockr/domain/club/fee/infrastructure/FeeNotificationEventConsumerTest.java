package com.official.lockr.domain.club.fee.infrastructure;

import com.official.lockr.domain.club.fee.domain.FeeClub;
import com.official.lockr.domain.club.fee.domain.event.UnpaidFeeNotifiedEvent;
import com.official.lockr.domain.notification.application.usecase.CreateFeeUnpaidNotificationUseCase;
import com.official.lockr.global.inbox.InMemoryIdempotentExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.PayloadApplicationEvent;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeeNotificationEventConsumerTest {

    private InMemoryIdempotentExecutor executor;
    private FeeClub feeClub;
    private CreateFeeUnpaidNotificationUseCase createFeeUnpaidNotificationUseCase;
    private FeeNotificationEventConsumer consumer;

    @BeforeEach
    void setUp() {
        executor = new InMemoryIdempotentExecutor();
        feeClub = mock(FeeClub.class);
        createFeeUnpaidNotificationUseCase = mock(CreateFeeUnpaidNotificationUseCase.class);
        consumer = new FeeNotificationEventConsumer(executor, feeClub, createFeeUnpaidNotificationUseCase);
    }

    @Test
    @DisplayName("동일한 eventId로 onApplicationEvent()를 두 번 호출해도 doHandle() 내부 로직은 1회만 실행되어야 한다")
    void shouldBeIdempotentOnDuplicateEventId() {
        when(feeClub.findUserIdsByMemberIds(anyString(), anyList()))
                .thenReturn(List.of("user-1", "user-2"));

        final UnpaidFeeNotifiedEvent event = new UnpaidFeeNotifiedEvent(
                "event-id-001",
                "notification-id-001",
                "club-1",
                2026,
                4,
                "sender-1",
                List.of("member-1", "member-2"),
                LocalDateTime.now()
        );

        consumer.onApplicationEvent(new PayloadApplicationEvent<>(consumer, event));
        consumer.onApplicationEvent(new PayloadApplicationEvent<>(consumer, event));

        verify(createFeeUnpaidNotificationUseCase, times(1)).create(any());
    }
}
