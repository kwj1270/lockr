package com.official.lockr.domain.home.banner.api.dto;

import com.official.lockr.domain.home.banner.application.command.ReorderBannerCommand;

public record ReorderBannerRequest(int displayOrder) {
    public ReorderBannerCommand toCommand(final String bannerId) {
        return new ReorderBannerCommand(bannerId, displayOrder);
    }
}
