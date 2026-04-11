package com.official.lockr.domain.home.banner.application.usecase;

import com.official.lockr.domain.home.banner.application.command.DeleteBannerCommand;

public interface DeleteBannerUseCase {
    void delete(DeleteBannerCommand command);
}
