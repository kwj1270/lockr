package com.official.lockr.domain.club.fee.domain;

import com.official.lockr.domain.club.fee.domain.event.UnpaidFeeNotifiedEvent;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.official.lockr.global.util.UlidUtils.generateUlid;

public class FeeNotification extends AggregateRoot {

    private final String id;
    private final String clubId;
    private final int year;
    private final int month;
    private final String sentBy;
    private final List<String> memberIds;
    private final LocalDateTime createdAt;

    public static FeeNotification init(final String clubId, final int year, final int month,
                                       final String sentBy, final List<String> memberIds) {
        final String id = generateUlid();
        final LocalDateTime now = LocalDateTime.now();
        final FeeNotification notification = new FeeNotification(id, clubId, year, month, sentBy, memberIds, now);
        notification.addEvent(new UnpaidFeeNotifiedEvent(clubId, year, month, sentBy, memberIds, now));
        return notification;
    }

    public FeeNotification(final String id, final String clubId, final int year, final int month,
                           final String sentBy, final List<String> memberIds, final LocalDateTime createdAt) {
        this.id = id;
        this.clubId = clubId;
        this.year = year;
        this.month = month;
        this.sentBy = sentBy;
        this.memberIds = memberIds;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getClubId() { return clubId; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public String getSentBy() { return sentBy; }
    public List<String> getMemberIds() { return memberIds; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final FeeNotification that = (FeeNotification) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
