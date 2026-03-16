package com.official.lockr.domain.shorts.application.command;

public record UploadShortsCommand(
        String userId,
        String clubId,
        String title,
        String description,
        String videoUrl,
        String thumbnailUrl,
        int duration
) {
}
