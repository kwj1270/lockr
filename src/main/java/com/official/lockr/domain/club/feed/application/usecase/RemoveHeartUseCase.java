package com.official.lockr.domain.club.feed.application.usecase;

import com.official.lockr.domain.club.feed.application.command.RemoveHeartCommand;
import com.official.lockr.domain.club.feed.domain.Feed;

public interface RemoveHeartUseCase {

    Feed removeHeart(RemoveHeartCommand command);
}
