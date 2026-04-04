package com.official.lockr.domain.club.fee.application;

import com.official.lockr.domain.club.club.domain.Club;
import com.official.lockr.domain.club.club.domain.ClubRepository;
import com.official.lockr.domain.club.club.domain.MemberRole;
import com.official.lockr.domain.club.fee.application.command.NotifyUnpaidFeeCommand;
import com.official.lockr.domain.club.fee.application.command.SetFeePolicyCommand;
import com.official.lockr.domain.club.fee.application.command.UpdateFeeRecordCommand;
import com.official.lockr.domain.club.fee.application.usecase.NotifyUnpaidFeeUseCase;
import com.official.lockr.domain.club.fee.application.usecase.SetFeePolicyUseCase;
import com.official.lockr.domain.club.fee.application.usecase.UpdateFeeRecordUseCase;
import com.official.lockr.domain.club.fee.domain.BankAccount;
import com.official.lockr.domain.club.fee.domain.FeePolicy;
import com.official.lockr.domain.club.fee.domain.FeePolicyRepository;
import com.official.lockr.domain.club.fee.domain.FeeRecord;
import com.official.lockr.domain.club.fee.domain.FeeRecordRepository;
import com.official.lockr.domain.club.fee.domain.FeeStatus;
import com.official.lockr.domain.club.fee.domain.event.UnpaidFeeNotifiedEvent;
import com.official.lockr.global.ddd.DomainEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class FeeService implements SetFeePolicyUseCase, UpdateFeeRecordUseCase, NotifyUnpaidFeeUseCase {

    private final ClubRepository clubRepository;
    private final FeePolicyRepository feePolicyRepository;
    private final FeeRecordRepository feeRecordRepository;
    private final DomainEventPublisher domainEventPublisher;

    public FeeService(final ClubRepository clubRepository,
                      final FeePolicyRepository feePolicyRepository,
                      final FeeRecordRepository feeRecordRepository,
                      final DomainEventPublisher domainEventPublisher) {
        this.clubRepository = clubRepository;
        this.feePolicyRepository = feePolicyRepository;
        this.feeRecordRepository = feeRecordRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public FeePolicy setPolicy(final SetFeePolicyCommand command) {
        final Club club = requireClub(command.clubId());
        requireFeePermission(club, command.userId());

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
        final Club club = requireClub(command.clubId());
        requireFeePermission(club, command.userId());
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
        final Club club = requireClub(command.clubId());
        requireFeePermission(club, command.userId());

        final int count = feeRecordRepository.countNotificationsByClubIdAndYearAndMonth(
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

        domainEventPublisher.publish(
                new UnpaidFeeNotifiedEvent(command.clubId(), command.year(), command.month(), unpaidMemberIds)
        );
    }

    private Club requireClub(final String clubId) {
        final Club club = clubRepository.findById(clubId);
        if (isNull(club)) {
            throw new IllegalStateException("클럽을 찾을 수 없습니다.");
        }
        return club;
    }

    private void requireFeePolicy(final String clubId) {
        if (isNull(feePolicyRepository.findByClubId(clubId))) {
            throw new IllegalStateException("회비 정책을 먼저 설정해주세요");
        }
    }

    private void requireFeePermission(final Club club, final String userId) {
        if (club.isPresidency(userId)) {
            return;
        }
        final boolean isTreasurer = club.getMembers().stream()
                .filter(m -> m.getUserId().equals(userId))
                .anyMatch(m -> m.getRole() == MemberRole.TREASURER);
        if (!isTreasurer) {
            throw new IllegalArgumentException("회비 관리 권한이 없습니다.");
        }
    }
}
