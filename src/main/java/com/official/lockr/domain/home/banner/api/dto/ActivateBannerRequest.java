package com.official.lockr.domain.home.banner.api.dto;

import com.official.lockr.domain.home.banner.application.command.ToggleBannerActiveCommand;

public record ActivateBannerRequest(boolean isActive) {
    public ToggleBannerActiveCommand toCommand(final String bannerId) {
        return new ToggleBannerActiveCommand(bannerId, isActive);
    }
}
