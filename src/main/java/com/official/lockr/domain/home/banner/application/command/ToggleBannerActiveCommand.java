package com.official.lockr.domain.home.banner.application.command;

public record ToggleBannerActiveCommand(String bannerId, boolean activate) {}
