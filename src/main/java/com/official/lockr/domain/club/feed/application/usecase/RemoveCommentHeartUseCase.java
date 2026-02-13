package com.official.lockr.domain.club.feed.application.usecase;

import com.official.lockr.domain.club.feed.application.command.RemoveCommentHeartCommand;
import com.official.lockr.domain.club.feed.domain.Feed;

public interface RemoveCommentHeartUseCase {

    Feed removeCommentHeart(RemoveCommentHeartCommand command);
}