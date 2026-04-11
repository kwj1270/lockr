package com.official.lockr.domain.home.banner.application.usecase;

import com.official.lockr.domain.home.banner.application.command.ToggleBannerActiveCommand;

public interface ToggleBannerActiveUseCase {
    void toggle(ToggleBannerActiveCommand command);
}
