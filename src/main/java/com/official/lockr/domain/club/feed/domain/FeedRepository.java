package com.official.lockr.domain.club.feed.domain;

import jakarta.annotation.Nullable;

import java.util.List;

public interface FeedRepository {

    @Nullable
    Feed findById(String id);

    List<Feed> findAllByClubId(String clubId);

    Feed save(Feed feed);
}
