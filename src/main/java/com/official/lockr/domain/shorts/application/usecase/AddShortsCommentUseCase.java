package com.official.lockr.domain.shorts.application.usecase;

import com.official.lockr.domain.shorts.application.command.AddShortsCommentCommand;
import com.official.lockr.domain.shorts.domain.ShortsComment;

public interface AddShortsCommentUseCase {

    ShortsComment add(AddShortsCommentCommand command);
}
