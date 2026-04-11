package com.official.lockr.domain.home.banner.domain;

import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.official.lockr.global.util.UlidUtils.generateUlid;

public class Banner extends AggregateRoot {

    private static final int MAX_ACTIVE_BANNERS_PER_PLACEMENT = 5;

    private final String id;
    private String title;
    private String subtitle;
    private BannerPlacement placement;
    private String imageUrl;
    private String actionUrl;
    private String actionRoute;
    private String bgColor;
    private String textColor;
    private String iconType;
    private int displayOrder;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Banner(final String id, final String title, final String subtitle,
                  final BannerPlacement placement, final String imageUrl,
                  final String actionUrl, final String actionRoute,
                  final String bgColor, final String textColor, final String iconType,
                  final int displayOrder, final boolean active,
                  final LocalDateTime createdAt, final LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.placement = placement;
        this.imageUrl = imageUrl;
        this.actionUrl = actionUrl;
        this.actionRoute = actionRoute;
        this.bgColor = bgColor;
        this.textColor = textColor;
        this.iconType = iconType;
        this.displayOrder = displayOrder;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Banner init(final String title, final String subtitle,
                              final BannerPlacement placement, final String imageUrl,
                              final String actionUrl, final String actionRoute,
                              final String bgColor, final String textColor,
                              final String iconType, final int displayOrder) {
        final LocalDateTime now = LocalDateTime.now();
        return new Banner(generateUlid(), title, subtitle, placement,
                imageUrl, actionUrl, actionRoute, bgColor, textColor, iconType,
                displayOrder, true, now, now);
    }

    public void update(final String title, final String subtitle,
                       final String imageUrl, final String actionUrl,
                       final String actionRoute, final String bgColor,
                       final String textColor, final String iconType,
                       final int displayOrder) {
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.actionUrl = actionUrl;
        this.actionRoute = actionRoute;
        this.bgColor = bgColor;
        this.textColor = textColor;
        this.iconType = iconType;
        this.displayOrder = displayOrder;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate(final int currentActiveCount) {
        if (this.active) {
            return;
        }
        if (currentActiveCount >= MAX_ACTIVE_BANNERS_PER_PLACEMENT) {
            throw new IllegalStateException("동일 placement 활성 배너는 최대 " + MAX_ACTIVE_BANNERS_PER_PLACEMENT + "개입니다.");
        }
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        if (!this.active) {
            return;
        }
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void reorder(final int newOrder) {
        this.displayOrder = newOrder;
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public BannerPlacement getPlacement() { return placement; }
    public String getImageUrl() { return imageUrl; }
    public String getActionUrl() { return actionUrl; }
    public String getActionRoute() { return actionRoute; }
    public String getBgColor() { return bgColor; }
    public String getTextColor() { return textColor; }
    public String getIconType() { return iconType; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Banner that = (Banner) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
