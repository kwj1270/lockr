package com.official.lockr.domain.club.feed.application.usecase;

import com.official.lockr.domain.club.feed.application.dto.RemoveHeartCommand;
import com.official.lockr.domain.club.feed.domain.Feed;

public interface RemoveHeartUseCase {

    Feed removeHeart(RemoveHeartCommand command);
}
