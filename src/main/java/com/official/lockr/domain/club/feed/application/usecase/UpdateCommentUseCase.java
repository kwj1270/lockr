package com.official.lockr.domain.club.feed.application.usecase;

import com.official.lockr.domain.club.feed.application.command.UpdateCommentCommand;
import com.official.lockr.domain.club.feed.domain.Feed;

public interface UpdateCommentUseCase {

    Feed updateComment(UpdateCommentCommand command);
}