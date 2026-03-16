package com.official.lockr.domain.shorts.application.usecase;

import com.official.lockr.domain.shorts.application.command.ReportShortsCommand;
import com.official.lockr.domain.shorts.domain.ShortsReport;

public interface ReportShortsUseCase {

    ShortsReport report(ReportShortsCommand command);
}
