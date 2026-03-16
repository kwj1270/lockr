package com.official.lockr.domain.club.feed.domain;

public interface FeedReportRepository {

    void save(String id, String feedId, String clubId, String reporterUserId, String reason);
}