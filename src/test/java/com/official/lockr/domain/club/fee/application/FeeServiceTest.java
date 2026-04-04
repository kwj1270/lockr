package com.official.lockr.domain.club.fee.application;

import com.official.lockr.domain.club.club.domain.Club;
import com.official.lockr.domain.club.club.domain.ClubRepository;
import com.official.lockr.domain.club.club.domain.Member;
import com.official.lockr.domain.club.club.domain.MemberRole;
import com.official.lockr.domain.club.fee.application.command.NotifyUnpaidFeeCommand;
import com.official.lockr.domain.club.fee.application.command.SetFeePolicyCommand;
import com.official.lockr.domain.club.fee.application.command.UpdateFeeRecordCommand;
import com.official.lockr.domain.club.fee.domain.FeePolicy;
import com.official.lockr.domain.club.fee.domain.FeePolicyRepository;
import com.official.lockr.domain.club.fee.domain.FeeRecord;
import com.official.lockr.domain.club.fee.domain.FeeRecordRepository;
import com.official.lockr.domain.club.fee.domain.FeeStatus;
import com.official.lockr.domain.club.fee.domain.event.UnpaidFeeNotifiedEvent;
import com.official.lockr.global.ddd.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeeServiceTest {

    private ClubRepository clubRepository;
    private FeePolicyRepository feePolicyRepository;
    private FeeRecordRepository feeRecordRepository;
    private DomainEventPublisher domainEventPublisher;
    private FeeService feeService;

    private static final String CLUB_ID = "club-001";
    private static final String PRESIDENT_USER_ID = "user-president";
    private static final String TREASURER_USER_ID = "user-treasurer";
    private static final String BASIC_USER_ID = "user-basic";
    private static final String MEMBER_ID = "member-target";

    @BeforeEach
    void setUp() {
        clubRepository = mock(ClubRepository.class);
        feePolicyRepository = mock(FeePolicyRepository.class);
        feeRecordRepository = mock(FeeRecordRepository.class);
        domainEventPublisher = mock(DomainEventPublisher.class);
        feeService = new FeeService(clubRepository, feePolicyRepository, feeRecordRepository, domainEventPublisher);
    }

    private Club clubWithMembers() {
        LocalDateTime now = LocalDateTime.now();
        List<Member> members = new ArrayList<>();
        members.add(new Member("m-president", PRESIDENT_USER_ID, MemberRole.PRESIDENT, CLUB_ID, "회장", null, now, now, null));
        members.add(new Member("m-treasurer", TREASURER_USER_ID, MemberRole.TREASURER, CLUB_ID, "회계", null, now, now, null));
        members.add(new Member("m-basic", BASIC_USER_ID, MemberRole.BASIC, CLUB_ID, "일반", null, now, now, null));
        return new Club(CLUB_ID, PRESIDENT_USER_ID, "FC 테스트", "FOOTBALL", "서울", "강남구", "테스트", null, null, members, now, now, null);
    }

    private void givenClubExists() {
        when(clubRepository.findById(CLUB_ID)).thenReturn(clubWithMembers());
    }

    // ============ setPolicy ============

    @Nested
    @DisplayName("setPolicy - 회비 정책 설정")
    class SetPolicy {

        @Test
        @DisplayName("회장이 정책을 처음 설정하면 FeePolicy가 생성된다")
        void shouldCreateWhenPresidentSetsForFirstTime() {
            givenClubExists();
            when(feePolicyRepository.findByClubId(CLUB_ID)).thenReturn(null);
            when(feePolicyRepository.save(any(FeePolicy.class))).thenAnswer(inv -> inv.getArgument(0));

            FeePolicy result = feeService.setPolicy(
                    new SetFeePolicyCommand(CLUB_ID, PRESIDENT_USER_ID, 50000, 10, "국민은행", "123-456", "홍길동"));

            assertThat(result.getAmount()).isEqualTo(50000);
            verify(feePolicyRepository).save(any(FeePolicy.class));
        }

        @Test
        @DisplayName("TREASURER도 정책을 설정할 수 있다")
        void shouldAllowTreasurer() {
            givenClubExists();
            when(feePolicyRepository.findByClubId(CLUB_ID)).thenReturn(null);
            when(feePolicyRepository.save(any(FeePolicy.class))).thenAnswer(inv -> inv.getArgument(0));

            FeePolicy result = feeService.setPolicy(
                    new SetFeePolicyCommand(CLUB_ID, TREASURER_USER_ID, 30000, 15, null, null, null));

            assertThat(result.getAmount()).isEqualTo(30000);
        }

        @Test
        @DisplayName("기존 정책이 있으면 업데이트한다")
        void shouldUpdateWhenPolicyExists() {
            givenClubExists();
            FeePolicy existing = FeePolicy.init(CLUB_ID, 30000, 10, null);
            when(feePolicyRepository.findByClubId(CLUB_ID)).thenReturn(existing);
            when(feePolicyRepository.save(any(FeePolicy.class))).thenAnswer(inv -> inv.getArgument(0));

            FeePolicy result = feeService.setPolicy(
                    new SetFeePolicyCommand(CLUB_ID, PRESIDENT_USER_ID, 50000, 20, null, null, null));

            assertThat(result.getAmount()).isEqualTo(50000);
            assertThat(result.getDueDay()).isEqualTo(20);
        }

        @Test
        @DisplayName("일반 회원이 설정하면 예외가 발생한다")
        void shouldThrowWhenBasicMember() {
            givenClubExists();

            assertThatThrownBy(() -> feeService.setPolicy(
                    new SetFeePolicyCommand(CLUB_ID, BASIC_USER_ID, 50000, 10, null, null, null)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("클럽이 없으면 예외가 발생한다")
        void shouldThrowWhenClubNotFound() {
            when(clubRepository.findById(CLUB_ID)).thenReturn(null);

            assertThatThrownBy(() -> feeService.setPolicy(
                    new SetFeePolicyCommand(CLUB_ID, PRESIDENT_USER_ID, 50000, 10, null, null, null)))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ============ updateRecord ============

    @Nested
    @DisplayName("updateRecord - 납부 상태 변경")
    class UpdateRecord {

        @Test
        @DisplayName("정책이 없으면 예외가 발생한다")
        void shouldThrowWhenPolicyNotFound() {
            givenClubExists();
            when(feePolicyRepository.findByClubId(CLUB_ID)).thenReturn(null);

            assertThatThrownBy(() -> feeService.updateRecord(
                    new UpdateFeeRecordCommand(CLUB_ID, PRESIDENT_USER_ID, MEMBER_ID, 2024, 1, FeeStatus.PAID, null)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("회비 정책을 먼저 설정해주세요");
        }

        @Test
        @DisplayName("기록이 없으면 새로 생성 후 PAID로 변경한다")
        void shouldCreateAndMarkPaid() {
            givenClubExists();
            when(feePolicyRepository.findByClubId(CLUB_ID)).thenReturn(FeePolicy.init(CLUB_ID, 50000, 10, null));
            when(feeRecordRepository.findByClubIdAndMemberIdAndYearAndMonth(CLUB_ID, MEMBER_ID, 2024, 1)).thenReturn(null);
            when(feeRecordRepository.save(any(FeeRecord.class))).thenAnswer(inv -> inv.getArgument(0));

            feeService.updateRecord(
                    new UpdateFeeRecordCommand(CLUB_ID, PRESIDENT_USER_ID, MEMBER_ID, 2024, 1, FeeStatus.PAID, null));

            verify(feeRecordRepository).save(any(FeeRecord.class));
        }

        @Test
        @DisplayName("기존 기록을 PAID로 변경한다")
        void shouldMarkPaid() {
            givenClubExists();
            FeeRecord record = FeeRecord.create(CLUB_ID, MEMBER_ID, 2024, 1);
            when(feePolicyRepository.findByClubId(CLUB_ID)).thenReturn(FeePolicy.init(CLUB_ID, 50000, 10, null));
            when(feeRecordRepository.findByClubIdAndMemberIdAndYearAndMonth(CLUB_ID, MEMBER_ID, 2024, 1)).thenReturn(record);
            when(feeRecordRepository.save(any(FeeRecord.class))).thenAnswer(inv -> inv.getArgument(0));

            feeService.updateRecord(
                    new UpdateFeeRecordCommand(CLUB_ID, PRESIDENT_USER_ID, MEMBER_ID, 2024, 1, FeeStatus.PAID, null));

            assertThat(record.getStatus()).isEqualTo(FeeStatus.PAID);
        }

        @Test
        @DisplayName("UNPAID로 변경한다")
        void shouldMarkUnpaid() {
            givenClubExists();
            FeeRecord record = FeeRecord.create(CLUB_ID, MEMBER_ID, 2024, 1);
            record.markPaid(PRESIDENT_USER_ID);
            when(feePolicyRepository.findByClubId(CLUB_ID)).thenReturn(FeePolicy.init(CLUB_ID, 50000, 10, null));
            when(feeRecordRepository.findByClubIdAndMemberIdAndYearAndMonth(CLUB_ID, MEMBER_ID, 2024, 1)).thenReturn(record);
            when(feeRecordRepository.save(any(FeeRecord.class))).thenAnswer(inv -> inv.getArgument(0));

            feeService.updateRecord(
                    new UpdateFeeRecordCommand(CLUB_ID, PRESIDENT_USER_ID, MEMBER_ID, 2024, 1, FeeStatus.UNPAID, null));

            assertThat(record.getStatus()).isEqualTo(FeeStatus.UNPAID);
        }

        @Test
        @DisplayName("memo를 업데이트한다")
        void shouldUpdateMemo() {
            givenClubExists();
            FeeRecord record = FeeRecord.create(CLUB_ID, MEMBER_ID, 2024, 1);
            when(feePolicyRepository.findByClubId(CLUB_ID)).thenReturn(FeePolicy.init(CLUB_ID, 50000, 10, null));
            when(feeRecordRepository.findByClubIdAndMemberIdAndYearAndMonth(CLUB_ID, MEMBER_ID, 2024, 1)).thenReturn(record);
            when(feeRecordRepository.save(any(FeeRecord.class))).thenAnswer(inv -> inv.getArgument(0));

            feeService.updateRecord(
                    new UpdateFeeRecordCommand(CLUB_ID, PRESIDENT_USER_ID, MEMBER_ID, 2024, 1, FeeStatus.PAID, "현금 납부"));

            assertThat(record.getMemo()).isEqualTo("현금 납부");
        }

        @Test
        @DisplayName("일반 회원이 변경하면 예외가 발생한다")
        void shouldThrowWhenBasicMemberUpdates() {
            givenClubExists();

            assertThatThrownBy(() -> feeService.updateRecord(
                    new UpdateFeeRecordCommand(CLUB_ID, BASIC_USER_ID, MEMBER_ID, 2024, 1, FeeStatus.PAID, null)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ============ notifyUnpaid ============

    @Nested
    @DisplayName("notifyUnpaid - 미납 알림")
    class NotifyUnpaid {

        @Test
        @DisplayName("3회 이상이면 예외가 발생한다")
        void shouldThrowWhenExceedsLimit() {
            givenClubExists();
            when(feeRecordRepository.countNotificationsByClubIdAndYearAndMonth(CLUB_ID, 2024, 1)).thenReturn(3);

            assertThatThrownBy(() -> feeService.notifyUnpaid(
                    new NotifyUnpaidFeeCommand(CLUB_ID, PRESIDENT_USER_ID, 2024, 1)))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("미납 회원이 있으면 UnpaidFeeNotifiedEvent를 발행한다")
        void shouldPublishEvent() {
            givenClubExists();
            FeeRecord unpaid1 = FeeRecord.create(CLUB_ID, "member-001", 2024, 1);
            FeeRecord unpaid2 = FeeRecord.create(CLUB_ID, "member-002", 2024, 1);
            FeeRecord paid = FeeRecord.create(CLUB_ID, "member-003", 2024, 1);
            paid.markPaid(PRESIDENT_USER_ID);

            when(feeRecordRepository.countNotificationsByClubIdAndYearAndMonth(CLUB_ID, 2024, 1)).thenReturn(1);
            when(feeRecordRepository.findByClubIdAndYearAndMonth(CLUB_ID, 2024, 1)).thenReturn(List.of(unpaid1, unpaid2, paid));

            feeService.notifyUnpaid(new NotifyUnpaidFeeCommand(CLUB_ID, PRESIDENT_USER_ID, 2024, 1));

            ArgumentCaptor<UnpaidFeeNotifiedEvent> captor = ArgumentCaptor.forClass(UnpaidFeeNotifiedEvent.class);
            verify(domainEventPublisher).publish(captor.capture());
            assertThat(captor.getValue().notifiedMemberIds()).containsExactly("member-001", "member-002");
        }

        @Test
        @DisplayName("미납 회원이 없으면 이벤트를 발행하지 않는다")
        void shouldNotPublishWhenNoUnpaid() {
            givenClubExists();
            FeeRecord paid = FeeRecord.create(CLUB_ID, "member-001", 2024, 1);
            paid.markPaid(PRESIDENT_USER_ID);

            when(feeRecordRepository.countNotificationsByClubIdAndYearAndMonth(CLUB_ID, 2024, 1)).thenReturn(0);
            when(feeRecordRepository.findByClubIdAndYearAndMonth(CLUB_ID, 2024, 1)).thenReturn(List.of(paid));

            feeService.notifyUnpaid(new NotifyUnpaidFeeCommand(CLUB_ID, PRESIDENT_USER_ID, 2024, 1));

            verify(domainEventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("일반 회원이 알림을 보내면 예외가 발생한다")
        void shouldThrowWhenBasicMemberNotifies() {
            givenClubExists();

            assertThatThrownBy(() -> feeService.notifyUnpaid(
                    new NotifyUnpaidFeeCommand(CLUB_ID, BASIC_USER_ID, 2024, 1)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
