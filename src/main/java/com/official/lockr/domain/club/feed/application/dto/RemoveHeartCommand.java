package com.official.lockr.domain.club.feed.application.dto;

public record RemoveHeartCommand(
        String feedId,
        String userId,
        String clubId) {
}
