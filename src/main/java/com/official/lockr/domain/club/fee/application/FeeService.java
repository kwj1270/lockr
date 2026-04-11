package com.official.lockr.domain.club.fee.application;

import com.official.lockr.domain.club.fee.application.command.DeferFeeRecordCommand;
import com.official.lockr.domain.club.fee.application.command.MarkPaidFeeRecordCommand;
import com.official.lockr.domain.club.fee.application.command.MarkUnpaidFeeRecordCommand;
import com.official.lockr.domain.club.fee.application.command.NotifyUnpaidFeeCommand;
import com.official.lockr.domain.club.fee.application.command.SetFeePolicyCommand;
import com.official.lockr.domain.club.fee.application.command.UpdateFeeRecordCommand;
import com.official.lockr.domain.club.fee.application.command.UpdateFeeRecordMemoCommand;
import com.official.lockr.domain.club.fee.application.usecase.DeferFeeRecordUseCase;
import com.official.lockr.domain.club.fee.application.usecase.MarkPaidFeeRecordUseCase;
import com.official.lockr.domain.club.fee.application.usecase.MarkUnpaidFeeRecordUseCase;
import com.official.lockr.domain.club.fee.application.usecase.NotifyUnpaidFeeUseCase;
import com.official.lockr.domain.club.fee.application.usecase.SetFeePolicyUseCase;
import com.official.lockr.domain.club.fee.application.usecase.UpdateFeeRecordMemoUseCase;
import com.official.lockr.domain.club.fee.application.usecase.UpdateFeeRecordUseCase;
import com.official.lockr.domain.club.fee.domain.BankAccount;
import com.official.lockr.domain.club.fee.domain.FeeClub;
import com.official.lockr.domain.club.fee.domain.FeeNotification;
import com.official.lockr.domain.club.fee.domain.FeeNotificationRepository;
import com.official.lockr.domain.club.fee.domain.FeePolicy;
import com.official.lockr.domain.club.fee.domain.FeePolicyRepository;
import com.official.lockr.domain.club.fee.domain.FeeRecord;
import com.official.lockr.domain.club.fee.domain.FeeRecordRepository;
import com.official.lockr.domain.club.fee.domain.FeeStatus;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class FeeService implements SetFeePolicyUseCase, UpdateFeeRecordUseCase, NotifyUnpaidFeeUseCase,
        MarkPaidFeeRecordUseCase, MarkUnpaidFeeRecordUseCase, UpdateFeeRecordMemoUseCase, DeferFeeRecordUseCase {

    private final FeeClub feeClub;
    private final FeePolicyRepository feePolicyRepository;
    private final FeeRecordRepository feeRecordRepository;
    private final FeeNotificationRepository feeNotificationRepository;

    public FeeService(final FeeClub feeClub,
                      final FeePolicyRepository feePolicyRepository,
                      final FeeRecordRepository feeRecordRepository,
                      final FeeNotificationRepository feeNotificationRepository) {
        this.feeClub = feeClub;
        this.feePolicyRepository = feePolicyRepository;
        this.feeRecordRepository = feeRecordRepository;
        this.feeNotificationRepository = feeNotificationRepository;
    }

    @Override
    public FeePolicy setPolicy(final SetFeePolicyCommand command) {
        feeClub.validateClubExists(command.clubId());
        requireFeePermission(command.clubId(), command.userId());

        final BankAccount bankAccount = BankAccount.of(command.bankName(), command.accountNumber(), command.accountHolder());
        final FeePolicy existing = feePolicyRepository.findByClubId(command.clubId());

        if (isNull(existing)) {
            return feePolicyRepository.save(FeePolicy.init(command.clubId(), command.amount(), command.dueDay(), bankAccount));
        }

        existing.updatePolicy(command.amount(), command.dueDay(), bankAccount);
        return feePolicyRepository.save(existing);
    }

    @Override
    public void updateRecord(final UpdateFeeRecordCommand command) {
        feeClub.validateClubExists(command.clubId());
        requireFeePermission(command.clubId(), command.userId());
        requireFeePolicy(command.clubId());

        FeeRecord record = feeRecordRepository.findByClubIdAndMemberIdAndYearAndMonth(
                command.clubId(), command.memberId(), command.year(), command.month()
        );
        if (isNull(record)) {
            record = FeeRecord.create(command.clubId(), command.memberId(), command.year(), command.month());
        }

        if (command.status() == FeeStatus.PAID) {
            record.markPaid(command.userId());
        } else if (command.status() == FeeStatus.UNPAID) {
            record.markUnpaid();
        }

        if (nonNull(command.memo())) {
            record.updateMemo(command.memo());
        }

        feeRecordRepository.save(record);
    }

    @Override
    public void notifyUnpaid(final NotifyUnpaidFeeCommand command) {
        feeClub.validateClubExists(command.clubId());
        requireFeePermission(command.clubId(), command.userId());

        final int count = feeNotificationRepository.countByClubIdAndYearAndMonth(
                command.clubId(), command.year(), command.month()
        );
        if (count >= 3) {
            throw new IllegalStateException("같은 월에 미납 알림은 최대 3회까지 가능합니다.");
        }

        final List<FeeRecord> records = feeRecordRepository.findByClubIdAndYearAndMonth(
                command.clubId(), command.year(), command.month()
        );

        final List<String> unpaidMemberIds = records.stream()
                .filter(r -> r.getStatus() == FeeStatus.UNPAID)
                .map(FeeRecord::getMemberId)
                .toList();

        if (unpaidMemberIds.isEmpty()) {
            return;
        }

        final FeeNotification notification = FeeNotification.init(
                command.clubId(), command.year(), command.month(), command.userId(), unpaidMemberIds
        );
        feeNotificationRepository.save(notification);
    }

    @Override
    public void markPaid(final MarkPaidFeeRecordCommand command) {
        feeClub.validateClubExists(command.clubId());
        requireFeePermission(command.clubId(), command.userId());

        FeeRecord record = feeRecordRepository.findByClubIdAndMemberIdAndYearAndMonth(
                command.clubId(), command.memberId(), command.year(), command.month()
        );
        if (isNull(record)) {
            record = FeeRecord.create(command.clubId(), command.memberId(), command.year(), command.month());
        }

        record.markPaid(command.userId());
        feeRecordRepository.save(record);
    }

    @Override
    public void markUnpaid(final MarkUnpaidFeeRecordCommand command) {
        feeClub.validateClubExists(command.clubId());
        requireFeePermission(command.clubId(), command.userId());

        FeeRecord record = feeRecordRepository.findByClubIdAndMemberIdAndYearAndMonth(
                command.clubId(), command.memberId(), command.year(), command.month()
        );
        if (isNull(record)) {
            record = FeeRecord.create(command.clubId(), command.memberId(), command.year(), command.month());
        }

        record.markUnpaid();
        feeRecordRepository.save(record);
    }

    @Override
    public void updateMemo(final UpdateFeeRecordMemoCommand command) {
        feeClub.validateClubExists(command.clubId());
        requireFeePermission(command.clubId(), command.userId());

        FeeRecord record = feeRecordRepository.findByClubIdAndMemberIdAndYearAndMonth(
                command.clubId(), command.memberId(), command.year(), command.month()
        );
        if (isNull(record)) {
            record = FeeRecord.create(command.clubId(), command.memberId(), command.year(), command.month());
        }

        record.updateMemo(command.memo());
        feeRecordRepository.save(record);
    }

    @Override
    public void defer(final DeferFeeRecordCommand command) {
        feeClub.validateClubExists(command.clubId());
        requireFeePermission(command.clubId(), command.userId());

        FeeRecord record = feeRecordRepository.findByClubIdAndMemberIdAndYearAndMonth(
                command.clubId(), command.memberId(), command.year(), command.month()
        );
        if (isNull(record)) {
            record = FeeRecord.create(command.clubId(), command.memberId(), command.year(), command.month());
        }

        record.markDeferred(command.userId());
        feeRecordRepository.save(record);
    }

    private void requireFeePolicy(final String clubId) {
        if (isNull(feePolicyRepository.findByClubId(clubId))) {
            throw new IllegalStateException("회비 정책을 먼저 설정해주세요");
        }
    }

    private void requireFeePermission(final String clubId, final String userId) {
        if (!feeClub.hasFeePermission(clubId, userId)) {
            throw new IllegalArgumentException("회비 관리 권한이 없습니다.");
        }
    }
}
