package com.official.lockr.domain.shorts.api.dto;

import com.official.lockr.domain.shorts.application.command.UploadShortsCommand;

public record UploadShortsRequest(
        String clubId,
        String title,
        String description,
        String videoUrl,
        String thumbnailUrl,
        int duration
) {
    public UploadShortsCommand toCommand(final String userId) {
        return new UploadShortsCommand(userId, clubId, title, description, videoUrl, thumbnailUrl, duration);
    }
}
