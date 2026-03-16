package com.official.lockr.domain.club.feed.application.usecase;

import com.official.lockr.domain.club.feed.application.command.DeleteFeedCommand;

public interface DeleteFeedUseCase {

    void delete(DeleteFeedCommand command);
}
