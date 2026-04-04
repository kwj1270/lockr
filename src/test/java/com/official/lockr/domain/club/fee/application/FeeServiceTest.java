package com.official.lockr.domain.club.fee.application;

import com.official.lockr.domain.club.fee.application.command.NotifyUnpaidFeeCommand;
import com.official.lockr.domain.club.fee.application.command.SetFeePolicyCommand;
import com.official.lockr.domain.club.fee.application.command.UpdateFeeRecordCommand;
import com.official.lockr.domain.club.fee.domain.FeeClub;
import com.official.lockr.domain.club.fee.domain.FeeNotification;
import com.official.lockr.domain.club.fee.domain.FeeNotificationRepository;
import com.official.lockr.domain.club.fee.domain.FeePolicy;
import com.official.lockr.domain.club.fee.domain.FeePolicyRepository;
import com.official.lockr.domain.club.fee.domain.FeeRecord;
import com.official.lockr.domain.club.fee.domain.FeeRecordRepository;
import com.official.lockr.domain.club.fee.domain.FeeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeeServiceTest {

    private FeeClub feeClub;
    private FeePolicyRepository feePolicyRepository;
    private FeeRecordRepository feeRecordRepository;
    private FeeNotificationRepository feeNotificationRepository;
    private FeeService feeService;

    private static final String CLUB_ID = "club-001";
    private static final String PRESIDENT_USER_ID = "user-president";
    private static final String TREASURER_USER_ID = "user-treasurer";
    private static final String BASIC_USER_ID = "user-basic";
    private static final String MEMBER_ID = "member-target";

    @BeforeEach
    void setUp() {
        feeClub = mock(FeeClub.class);
        feePolicyRepository = mock(FeePolicyRepository.class);
        feeRecordRepository = mock(FeeRecordRepository.class);
        feeNotificationRepository = mock(FeeNotificationRepository.class);
        feeService = new FeeService(feeClub, feePolicyRepository, feeRecordRepository, feeNotificationRepository);
    }

    private void givenClubExists() {
        // validateClubExists does nothing (no exception)
    }

    private void givenPresidentPermission() {
        when(feeClub.hasFeePermission(CLUB_ID, PRESIDENT_USER_ID)).thenReturn(true);
    }

    private void givenTreasurerPermission() {
        when(feeClub.hasFeePermission(CLUB_ID, TREASURER_USER_ID)).thenReturn(true);
    }

    private void givenNoPermission() {
        when(feeClub.hasFeePermission(CLUB_ID, BASIC_USER_ID)).thenReturn(false);
    }

    // ============ setPolicy ============

    @Nested
    @DisplayName("setPolicy - 회비 정책 설정")
    class SetPolicy {

        @Test
        @DisplayName("회장이 정책을 처음 설정하면 FeePolicy가 생성된다")
        void shouldCreateWhenPresidentSetsForFirstTime() {
            givenClubExists();
            givenPresidentPermission();
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
            givenTreasurerPermission();
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
            givenPresidentPermission();
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
            givenNoPermission();

            assertThatThrownBy(() -> feeService.setPolicy(
                    new SetFeePolicyCommand(CLUB_ID, BASIC_USER_ID, 50000, 10, null, null, null)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("클럽이 없으면 예외가 발생한다")
        void shouldThrowWhenClubNotFound() {
            doThrow(new IllegalStateException("클럽을 찾을 수 없습니다."))
                    .when(feeClub).validateClubExists(CLUB_ID);

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
            givenPresidentPermission();
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
            givenPresidentPermission();
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
            givenPresidentPermission();
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
            givenPresidentPermission();
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
            givenPresidentPermission();
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
            givenNoPermission();

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
            givenPresidentPermission();
            when(feeNotificationRepository.countByClubIdAndYearAndMonth(CLUB_ID, 2024, 1)).thenReturn(3);

            assertThatThrownBy(() -> feeService.notifyUnpaid(
                    new NotifyUnpaidFeeCommand(CLUB_ID, PRESIDENT_USER_ID, 2024, 1)))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("미납 회원이 있으면 FeeNotification을 저장한다")
        void shouldSaveNotificationWhenUnpaidExists() {
            givenClubExists();
            givenPresidentPermission();
            FeeRecord unpaid1 = FeeRecord.create(CLUB_ID, "member-001", 2024, 1);
            FeeRecord unpaid2 = FeeRecord.create(CLUB_ID, "member-002", 2024, 1);
            FeeRecord paid = FeeRecord.create(CLUB_ID, "member-003", 2024, 1);
            paid.markPaid(PRESIDENT_USER_ID);

            when(feeNotificationRepository.countByClubIdAndYearAndMonth(CLUB_ID, 2024, 1)).thenReturn(1);
            when(feeRecordRepository.findByClubIdAndYearAndMonth(CLUB_ID, 2024, 1)).thenReturn(List.of(unpaid1, unpaid2, paid));
            when(feeNotificationRepository.save(any(FeeNotification.class))).thenAnswer(inv -> inv.getArgument(0));

            feeService.notifyUnpaid(new NotifyUnpaidFeeCommand(CLUB_ID, PRESIDENT_USER_ID, 2024, 1));

            verify(feeNotificationRepository).save(any(FeeNotification.class));
        }

        @Test
        @DisplayName("미납 회원이 없으면 알림을 저장하지 않는다")
        void shouldNotSaveWhenNoUnpaid() {
            givenClubExists();
            givenPresidentPermission();
            FeeRecord paid = FeeRecord.create(CLUB_ID, "member-001", 2024, 1);
            paid.markPaid(PRESIDENT_USER_ID);

            when(feeNotificationRepository.countByClubIdAndYearAndMonth(CLUB_ID, 2024, 1)).thenReturn(0);
            when(feeRecordRepository.findByClubIdAndYearAndMonth(CLUB_ID, 2024, 1)).thenReturn(List.of(paid));

            feeService.notifyUnpaid(new NotifyUnpaidFeeCommand(CLUB_ID, PRESIDENT_USER_ID, 2024, 1));

            verify(feeNotificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("미납자 5명일 때에도 첫 알림 발송 가능해야 한다")
        void shouldAllowFirstNotificationWhenFiveUnpaidMembers() {
            givenClubExists();
            givenPresidentPermission();
            List<FeeRecord> unpaidRecords = List.of(
                    FeeRecord.create(CLUB_ID, "member-001", 2024, 1),
                    FeeRecord.create(CLUB_ID, "member-002", 2024, 1),
                    FeeRecord.create(CLUB_ID, "member-003", 2024, 1),
                    FeeRecord.create(CLUB_ID, "member-004", 2024, 1),
                    FeeRecord.create(CLUB_ID, "member-005", 2024, 1)
            );

            when(feeNotificationRepository.countByClubIdAndYearAndMonth(CLUB_ID, 2024, 1)).thenReturn(0);
            when(feeRecordRepository.findByClubIdAndYearAndMonth(CLUB_ID, 2024, 1)).thenReturn(unpaidRecords);
            when(feeNotificationRepository.save(any(FeeNotification.class))).thenAnswer(inv -> inv.getArgument(0));

            feeService.notifyUnpaid(new NotifyUnpaidFeeCommand(CLUB_ID, PRESIDENT_USER_ID, 2024, 1));

            verify(feeNotificationRepository).save(any(FeeNotification.class));
        }

        @Test
        @DisplayName("일반 회원이 알림을 보내면 예외가 발생한다")
        void shouldThrowWhenBasicMemberNotifies() {
            givenClubExists();
            givenNoPermission();

            assertThatThrownBy(() -> feeService.notifyUnpaid(
                    new NotifyUnpaidFeeCommand(CLUB_ID, BASIC_USER_ID, 2024, 1)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
