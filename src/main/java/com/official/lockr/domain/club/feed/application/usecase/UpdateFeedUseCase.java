package com.official.lockr.domain.club.feed.application.usecase;

import com.official.lockr.domain.club.feed.application.dto.UpdateFeedCommand;
import com.official.lockr.domain.club.feed.domain.Feed;

public interface UpdateFeedUseCase {

    Feed update(UpdateFeedCommand command);
}
