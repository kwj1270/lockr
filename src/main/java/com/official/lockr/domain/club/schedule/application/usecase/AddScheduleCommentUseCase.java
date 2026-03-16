package com.official.lockr.domain.club.schedule.application.usecase;

import com.official.lockr.domain.club.schedule.application.command.AddScheduleCommentCommand;

public interface AddScheduleCommentUseCase {

    void addComment(AddScheduleCommentCommand command);
}
