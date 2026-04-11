package com.official.lockr.domain.home.banner.application.usecase;

import com.official.lockr.domain.home.banner.application.command.ReorderBannerCommand;

public interface ReorderBannerUseCase {
    void reorder(ReorderBannerCommand command);
}
