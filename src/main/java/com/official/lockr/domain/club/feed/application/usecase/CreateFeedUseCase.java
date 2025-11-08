package com.official.lockr.domain.club.feed.application.usecase;

import com.official.lockr.domain.club.feed.application.dto.CreateFeedCommand;
import com.official.lockr.domain.club.feed.domain.Feed;

public interface CreateFeedUseCase {

    Feed create(CreateFeedCommand command);
}
