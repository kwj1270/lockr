package com.official.lockr.domain.shorts.application.usecase;

import com.official.lockr.domain.shorts.application.command.DeleteShortsCommentCommand;

public interface DeleteShortsCommentUseCase {

    void delete(DeleteShortsCommentCommand command);
}
