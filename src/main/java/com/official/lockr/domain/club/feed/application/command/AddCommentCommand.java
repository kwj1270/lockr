package com.official.lockr.domain.club.feed.application.command;

import java.util.List;

public record AddCommentCommand(
        String feedId,
        String userId,
        String clubId,
        String content,
        List<String> imageUrls,
        List<String> videoUrls
) {
}
