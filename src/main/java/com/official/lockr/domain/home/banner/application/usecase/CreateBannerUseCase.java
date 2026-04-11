package com.official.lockr.domain.home.banner.application.usecase;

import com.official.lockr.domain.home.banner.application.command.CreateBannerCommand;
import com.official.lockr.domain.home.banner.domain.Banner;

public interface CreateBannerUseCase {
    Banner create(CreateBannerCommand command);
}
