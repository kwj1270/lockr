package com.official.lockr.domain.club.feed.application.usecase;

import com.official.lockr.domain.club.feed.application.dto.AddCommentCommand;
import com.official.lockr.domain.club.feed.domain.Feed;

public interface AddCommentUseCase {

    Feed addComment(AddCommentCommand command);
}
