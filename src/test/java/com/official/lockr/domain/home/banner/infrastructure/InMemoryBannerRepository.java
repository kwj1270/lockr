package com.official.lockr.domain.home.banner.infrastructure;

import com.official.lockr.domain.home.banner.domain.Banner;
import com.official.lockr.domain.home.banner.domain.BannerPlacement;
import com.official.lockr.domain.home.banner.domain.BannerRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryBannerRepository implements BannerRepository {

    private final Map<String, Banner> store = new HashMap<>();

    @Override
    public Banner save(final Banner banner) {
        store.put(banner.getId(), deepCopy(banner));
        return banner;
    }

    @Override
    public Banner findById(final String id) {
        final Banner banner = store.get(id);
        if (banner == null) {
            return null;
        }
        return deepCopy(banner);
    }

    @Override
    public void delete(final String id) {
        store.remove(id);
    }

    @Override
    public int countActiveBannersByPlacement(final BannerPlacement placement) {
        return (int) store.values().stream()
                .filter(b -> b.getPlacement() == placement && b.isActive())
                .count();
    }

    public List<Banner> findAll() {
        return store.values().stream()
                .map(InMemoryBannerRepository::deepCopy)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public void clear() {
        store.clear();
    }

    private static Banner deepCopy(final Banner banner) {
        return new Banner(
                banner.getId(),
                banner.getTitle(),
                banner.getSubtitle(),
                banner.getPlacement(),
                banner.getImageUrl(),
                banner.getActionUrl(),
                banner.getActionRoute(),
                banner.getBgColor(),
                banner.getTextColor(),
                banner.getIconType(),
                banner.getDisplayOrder(),
                banner.isActive(),
                banner.getCreatedAt(),
                banner.getUpdatedAt()
        );
    }
}
