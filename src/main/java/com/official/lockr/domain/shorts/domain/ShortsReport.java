package com.official.lockr.domain.shorts.domain;

import java.time.LocalDateTime;

public class ShortsReport {

    private final String id;
    private final String shortsId;
    private final String userId;
    private final ReportReason reason;
    private final String detail;
    private final LocalDateTime createdAt;

    public ShortsReport(final String id, final String shortsId, final String userId,
                        final ReportReason reason, final String detail, final LocalDateTime createdAt) {
        this.id = id;
        this.shortsId = shortsId;
        this.userId = userId;
        this.reason = reason;
        this.detail = detail;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getShortsId() {
        return shortsId;
    }

    public String getUserId() {
        return userId;
    }

    public ReportReason getReason() {
        return reason;
    }

    public String getDetail() {
        return detail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
