package com.official.lockr.domain.club.feed.application.usecase;

import com.official.lockr.domain.club.feed.application.command.CreateFeedCommand;
import com.official.lockr.domain.club.feed.domain.Feed;

public interface CreateFeedUseCase {

    Feed create(CreateFeedCommand command);
}
