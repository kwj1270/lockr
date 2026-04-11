package com.official.lockr.domain.club.fee.domain;

import com.official.lockr.domain.club.fee.domain.event.FeeRecordMarkedPaidEvent;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.official.lockr.global.util.UlidUtils.generateUlid;

public class FeeRecord extends AggregateRoot {

    private final String id;
    private final String clubId;
    private final String memberId;
    private final int year;
    private final int month;
    private FeeStatus status;
    private LocalDateTime paidAt;
    private String updatedBy;
    private String memo;

    public FeeRecord(final String id, final String clubId, final String memberId,
                     final int year, final int month, final FeeStatus status,
                     final LocalDateTime paidAt, final String updatedBy, final String memo) {
        this.id = id;
        this.clubId = clubId;
        this.memberId = memberId;
        this.year = year;
        this.month = month;
        this.status = status;
        this.paidAt = paidAt;
        this.updatedBy = updatedBy;
        this.memo = memo;
    }

    public static FeeRecord create(final String clubId, final String memberId, final int year, final int month) {
        return new FeeRecord(generateUlid(), clubId, memberId, year, month, FeeStatus.UNPAID, null, null, null);
    }

    public void markPaid(final String updatedBy) {
        if (this.status == FeeStatus.PAID) {
            return;
        }
        this.status = FeeStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
        addEvent(new FeeRecordMarkedPaidEvent(this.id, this.clubId, this.memberId, this.year, this.month));
    }

    public void markUnpaid() {
        this.status = FeeStatus.UNPAID;
        this.paidAt = null;
        this.updatedBy = null;
    }

    public void updateMemo(final String memo) {
        this.memo = memo;
    }

    public String getId() { return id; }
    public String getClubId() { return clubId; }
    public String getMemberId() { return memberId; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public FeeStatus getStatus() { return status; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public String getUpdatedBy() { return updatedBy; }
    public String getMemo() { return memo; }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final FeeRecord that = (FeeRecord) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
