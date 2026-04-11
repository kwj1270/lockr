package com.official.lockr.domain.home.banner.infrastructure;

import com.official.lockr.domain.home.banner.domain.Banner;
import com.official.lockr.domain.home.banner.domain.BannerPlacement;
import com.official.lockr.domain.home.banner.domain.BannerRepository;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.BannersDao;
import org.jooq.generated.tables.pojos.BannersEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static org.jooq.generated.tables.BannersJOOQEntity.BANNERS;

@Repository
public class JOOQBannerRepository implements BannerRepository {

    private final BannersDao bannersDao;

    public JOOQBannerRepository(final Configuration configuration) {
        this.bannersDao = new BannersDao(configuration);
    }

    @Transactional
    @Override
    public Banner save(final Banner banner) {
        bannersDao.ctx()
                .insertInto(BANNERS)
                .set(BANNERS.ID, banner.getId())
                .set(BANNERS.TITLE, banner.getTitle())
                .set(BANNERS.SUBTITLE, banner.getSubtitle())
                .set(BANNERS.PLACEMENT, banner.getPlacement().name())
                .set(BANNERS.IMAGE_URL, banner.getImageUrl())
                .set(BANNERS.ACTION_URL, banner.getActionUrl())
                .set(BANNERS.ACTION_ROUTE, banner.getActionRoute())
                .set(BANNERS.BG_COLOR, banner.getBgColor())
                .set(BANNERS.TEXT_COLOR, banner.getTextColor())
                .set(BANNERS.ICON_TYPE, banner.getIconType())
                .set(BANNERS.DISPLAY_ORDER, banner.getDisplayOrder())
                .set(BANNERS.IS_ACTIVE, banner.isActive())
                .set(BANNERS.CREATED_AT, banner.getCreatedAt())
                .set(BANNERS.UPDATED_AT, banner.getUpdatedAt())
                .onDuplicateKeyUpdate()
                .set(BANNERS.TITLE, banner.getTitle())
                .set(BANNERS.SUBTITLE, banner.getSubtitle())
                .set(BANNERS.PLACEMENT, banner.getPlacement().name())
                .set(BANNERS.IMAGE_URL, banner.getImageUrl())
                .set(BANNERS.ACTION_URL, banner.getActionUrl())
                .set(BANNERS.ACTION_ROUTE, banner.getActionRoute())
                .set(BANNERS.BG_COLOR, banner.getBgColor())
                .set(BANNERS.TEXT_COLOR, banner.getTextColor())
                .set(BANNERS.ICON_TYPE, banner.getIconType())
                .set(BANNERS.DISPLAY_ORDER, banner.getDisplayOrder())
                .set(BANNERS.IS_ACTIVE, banner.isActive())
                .set(BANNERS.UPDATED_AT, banner.getUpdatedAt())
                .execute();
        return banner;
    }

    @Override
    public Banner findById(final String id) {
        final BannersEntity entity = bannersDao.ctx()
                .selectFrom(BANNERS)
                .where(BANNERS.ID.eq(id))
                .fetchOneInto(BannersEntity.class);
        if (entity == null) {
            return null;
        }
        return domain(entity);
    }

    @Transactional
    @Override
    public void delete(final String id) {
        bannersDao.ctx()
                .deleteFrom(BANNERS)
                .where(BANNERS.ID.eq(id))
                .execute();
    }

    @Override
    public int countActiveBannersByPlacement(final BannerPlacement placement) {
        return bannersDao.ctx()
                .selectCount()
                .from(BANNERS)
                .where(BANNERS.PLACEMENT.eq(placement.name()))
                .and(BANNERS.IS_ACTIVE.eq(true))
                .fetchOne(0, int.class);
    }

    private static Banner domain(final BannersEntity entity) {
        return new Banner(
                entity.getId(),
                entity.getTitle(),
                entity.getSubtitle(),
                BannerPlacement.valueOf(entity.getPlacement()),
                entity.getImageUrl(),
                entity.getActionUrl(),
                entity.getActionRoute(),
                entity.getBgColor(),
                entity.getTextColor(),
                entity.getIconType(),
                entity.getDisplayOrder(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
