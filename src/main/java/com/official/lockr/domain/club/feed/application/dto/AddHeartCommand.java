package com.official.lockr.domain.club.feed.application.dto;

public record AddHeartCommand(
        String feedId,
        String userId,
        String clubId) {
}
