package com.official.lockr.domain.home.banner.application.usecase;

import com.official.lockr.domain.home.banner.application.command.UpdateBannerCommand;

public interface UpdateBannerUseCase {
    void update(UpdateBannerCommand command);
}
