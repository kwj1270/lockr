package com.official.lockr.domain.club.feed.application.usecase;

import com.official.lockr.domain.club.feed.application.command.ReportFeedCommand;

public interface ReportFeedUseCase {

    void report(ReportFeedCommand command);
}