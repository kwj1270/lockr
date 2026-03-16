package com.official.lockr.domain.club.feed.application.usecase;

import com.official.lockr.domain.club.feed.application.dto.AddHeartCommand;
import com.official.lockr.domain.club.feed.domain.Feed;

public interface AddHeartUseCase {

    Feed addHeart(AddHeartCommand command);
}
