package com.official.lockr.domain.home.banner.api;

import com.official.lockr.domain.home.banner.api.dto.AdminBannerResponse;
import com.official.lockr.domain.home.banner.api.dto.BannerResponse;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.BannersDao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.jooq.generated.tables.BannersJOOQEntity.BANNERS;

@RestController
@RequestMapping("/api/v1/banners")
public class BannerQueryApi {

    private final BannersDao bannersDao;

    public BannerQueryApi(final Configuration configuration) {
        this.bannersDao = new BannersDao(configuration);
    }

    @GetMapping
    public ResponseEntity<BannersResponse> banners(
            @RequestParam final String placement
    ) {
        final List<BannerResponse> banners = bannersDao.ctx()
                .select(
                        BANNERS.ID,
                        BANNERS.TITLE,
                        BANNERS.SUBTITLE,
                        BANNERS.IMAGE_URL,
                        BANNERS.ACTION_URL,
                        BANNERS.ACTION_ROUTE,
                        BANNERS.BG_COLOR,
                        BANNERS.TEXT_COLOR,
                        BANNERS.ICON_TYPE,
                        BANNERS.DISPLAY_ORDER,
                        BANNERS.IS_ACTIVE
                )
                .from(BANNERS)
                .where(BANNERS.PLACEMENT.eq(placement))
                .and(BANNERS.IS_ACTIVE.eq(true))
                .orderBy(BANNERS.DISPLAY_ORDER.asc())
                .fetch()
                .map(record -> new BannerResponse(
                        record.get(BANNERS.ID),
                        record.get(BANNERS.TITLE),
                        record.get(BANNERS.SUBTITLE),
                        record.get(BANNERS.IMAGE_URL),
                        record.get(BANNERS.ACTION_URL),
                        record.get(BANNERS.ACTION_ROUTE),
                        record.get(BANNERS.BG_COLOR),
                        record.get(BANNERS.TEXT_COLOR),
                        record.get(BANNERS.ICON_TYPE),
                        record.get(BANNERS.DISPLAY_ORDER),
                        record.get(BANNERS.IS_ACTIVE)
                ));

        return ResponseEntity.ok(new BannersResponse(banners));
    }

    @GetMapping("/admin")
    public ResponseEntity<AdminBannersResponse> adminBanners(
            @RequestParam final String placement
    ) {
        final List<AdminBannerResponse> banners = bannersDao.ctx()
                .selectFrom(BANNERS)
                .where(BANNERS.PLACEMENT.eq(placement))
                .orderBy(BANNERS.DISPLAY_ORDER.asc())
                .fetch()
                .map(record -> new AdminBannerResponse(
                        record.get(BANNERS.ID),
                        record.get(BANNERS.TITLE),
                        record.get(BANNERS.SUBTITLE),
                        record.get(BANNERS.IMAGE_URL),
                        record.get(BANNERS.ACTION_URL),
                        record.get(BANNERS.ACTION_ROUTE),
                        record.get(BANNERS.BG_COLOR),
                        record.get(BANNERS.TEXT_COLOR),
                        record.get(BANNERS.ICON_TYPE),
                        record.get(BANNERS.DISPLAY_ORDER),
                        record.get(BANNERS.IS_ACTIVE),
                        record.get(BANNERS.CREATED_AT),
                        record.get(BANNERS.UPDATED_AT)
                ));

        return ResponseEntity.ok(new AdminBannersResponse(banners));
    }

    record BannersResponse(List<BannerResponse> banners) {}
    record AdminBannersResponse(List<AdminBannerResponse> banners) {}
}
