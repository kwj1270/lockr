package com.official.lockr.domain.club.feed.application.usecase;

import com.official.lockr.domain.club.feed.application.command.AddCommentHeartCommand;
import com.official.lockr.domain.club.feed.domain.Feed;

public interface AddCommentHeartUseCase {

    Feed addCommentHeart(AddCommentHeartCommand command);
}