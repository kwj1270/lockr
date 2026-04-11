package com.official.lockr.domain.home.banner.domain;

public interface BannerRepository {

    Banner save(Banner banner);

    Banner findById(String id);

    void delete(String id);

    int countActiveBannersByPlacement(BannerPlacement placement);
}
