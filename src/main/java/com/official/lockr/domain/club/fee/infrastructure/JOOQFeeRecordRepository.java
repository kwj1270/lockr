package com.official.lockr.domain.club.fee.infrastructure;

import com.official.lockr.domain.club.fee.domain.FeeRecord;
import com.official.lockr.domain.club.fee.domain.FeeRecordRepository;
import com.official.lockr.domain.club.fee.domain.FeeStatus;

import com.official.lockr.global.ddd.DomainEventPublisher;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.FeeRecordsDao;
import org.jooq.generated.tables.pojos.FeeRecordsEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.jooq.generated.tables.FeeRecordsJOOQEntity.FEE_RECORDS;

@Repository
public class JOOQFeeRecordRepository implements FeeRecordRepository {

    private final FeeRecordsDao feeRecordsDao;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQFeeRecordRepository(final Configuration configuration,
                                   final DomainEventPublisher domainEventPublisher) {
        this.feeRecordsDao = new FeeRecordsDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Transactional
    @Override
    public FeeRecord save(final FeeRecord record) {
        feeRecordsDao.ctx()
                .insertInto(FEE_RECORDS)
                .set(FEE_RECORDS.ID, record.getId())
                .set(FEE_RECORDS.CLUB_ID, record.getClubId())
                .set(FEE_RECORDS.MEMBER_ID, record.getMemberId())
                .set(FEE_RECORDS.YEAR, record.getYear())
                .set(FEE_RECORDS.MONTH, record.getMonth())
                .set(FEE_RECORDS.STATUS, record.getStatus().name())
                .set(FEE_RECORDS.MEMO, record.getMemo())
                .set(FEE_RECORDS.UPDATED_BY, record.getUpdatedBy())
                .onDuplicateKeyUpdate()
                .set(FEE_RECORDS.STATUS, record.getStatus().name())
                .set(FEE_RECORDS.MEMO, record.getMemo())
                .set(FEE_RECORDS.UPDATED_BY, record.getUpdatedBy())
                .execute();
        record.publish(domainEventPublisher);
        return record;
    }

    @Override
    public List<FeeRecord> findByClubIdAndYearAndMonth(final String clubId, final int year, final int month) {
        return feeRecordsDao.ctx()
                .selectFrom(FEE_RECORDS)
                .where(FEE_RECORDS.CLUB_ID.eq(clubId))
                .and(FEE_RECORDS.YEAR.eq(year))
                .and(FEE_RECORDS.MONTH.eq(month))
                .fetchInto(FeeRecordsEntity.class)
                .stream()
                .map(JOOQFeeRecordRepository::domain)
                .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public FeeRecord findByClubIdAndMemberIdAndYearAndMonth(final String clubId, final String memberId,
                                                             final int year, final int month) {
        final var entity = feeRecordsDao.ctx()
                .selectFrom(FEE_RECORDS)
                .where(FEE_RECORDS.CLUB_ID.eq(clubId))
                .and(FEE_RECORDS.MEMBER_ID.eq(memberId))
                .and(FEE_RECORDS.YEAR.eq(year))
                .and(FEE_RECORDS.MONTH.eq(month))
                .fetchOneInto(FeeRecordsEntity.class);
        if (Objects.isNull(entity)) {
            return null;
        }
        return domain(entity);
    }

    private static FeeRecord domain(final FeeRecordsEntity entity) {
        return new FeeRecord(
                entity.getId(),
                entity.getClubId(),
                entity.getMemberId(),
                entity.getYear(),
                entity.getMonth(),
                FeeStatus.valueOf(entity.getStatus()),
                entity.getUpdatedBy(),
                entity.getMemo()
        );
    }
}
