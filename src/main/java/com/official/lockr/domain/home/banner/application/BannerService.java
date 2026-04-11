package com.official.lockr.domain.home.banner.application;

import com.official.lockr.domain.home.banner.application.command.CreateBannerCommand;
import com.official.lockr.domain.home.banner.application.command.DeleteBannerCommand;
import com.official.lockr.domain.home.banner.application.command.ReorderBannerCommand;
import com.official.lockr.domain.home.banner.application.command.ToggleBannerActiveCommand;
import com.official.lockr.domain.home.banner.application.command.UpdateBannerCommand;
import com.official.lockr.domain.home.banner.application.usecase.CreateBannerUseCase;
import com.official.lockr.domain.home.banner.application.usecase.DeleteBannerUseCase;
import com.official.lockr.domain.home.banner.application.usecase.ReorderBannerUseCase;
import com.official.lockr.domain.home.banner.application.usecase.ToggleBannerActiveUseCase;
import com.official.lockr.domain.home.banner.application.usecase.UpdateBannerUseCase;
import com.official.lockr.domain.home.banner.domain.Banner;
import com.official.lockr.domain.home.banner.domain.BannerPlacement;
import com.official.lockr.domain.home.banner.domain.BannerRepository;
import org.springframework.stereotype.Service;

@Service
public class BannerService implements CreateBannerUseCase, UpdateBannerUseCase,
        DeleteBannerUseCase, ToggleBannerActiveUseCase, ReorderBannerUseCase {

    private final BannerRepository bannerRepository;

    public BannerService(final BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    @Override
    public Banner create(final CreateBannerCommand command) {
        final Banner banner = Banner.init(
                command.title(), command.subtitle(),
                BannerPlacement.valueOf(command.placement()),
                command.imageUrl(), command.actionUrl(), command.actionRoute(),
                command.bgColor(), command.textColor(), command.iconType(),
                command.displayOrder()
        );
        return bannerRepository.save(banner);
    }

    @Override
    public void update(final UpdateBannerCommand command) {
        final Banner banner = getBannerOrThrow(command.bannerId());
        banner.update(
                command.title(), command.subtitle(),
                command.imageUrl(), command.actionUrl(), command.actionRoute(),
                command.bgColor(), command.textColor(), command.iconType(),
                command.displayOrder()
        );
        bannerRepository.save(banner);
    }

    @Override
    public void delete(final DeleteBannerCommand command) {
        getBannerOrThrow(command.bannerId());
        bannerRepository.delete(command.bannerId());
    }

    @Override
    public void toggle(final ToggleBannerActiveCommand command) {
        final Banner banner = getBannerOrThrow(command.bannerId());
        if (command.activate()) {
            final int activeCount = bannerRepository.countActiveBannersByPlacement(banner.getPlacement());
            banner.activate(activeCount);
        } else {
            banner.deactivate();
        }
        bannerRepository.save(banner);
    }

    @Override
    public void reorder(final ReorderBannerCommand command) {
        final Banner banner = getBannerOrThrow(command.bannerId());
        banner.reorder(command.displayOrder());
        bannerRepository.save(banner);
    }

    private Banner getBannerOrThrow(final String bannerId) {
        final Banner banner = bannerRepository.findById(bannerId);
        if (banner == null) {
            throw new IllegalArgumentException("배너를 찾을 수 없습니다.");
        }
        return banner;
    }
}
