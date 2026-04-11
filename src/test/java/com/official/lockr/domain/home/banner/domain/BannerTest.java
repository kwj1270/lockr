package com.official.lockr.domain.home.banner.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BannerTest {

    @DisplayName("배너 생성 시 기본값이 올바르게 설정된다")
    @Test
    void shouldCreateBannerWithDefaults() {
        // when
        final Banner banner = Banner.init(
                "제목", "부제목", BannerPlacement.HOME_TOP,
                null, null, "/calendar",
                "#1F2937", "#FFFFFF", "calendar", 1
        );

        // then
        assertThat(banner.getId()).isNotNull();
        assertThat(banner.getTitle()).isEqualTo("제목");
        assertThat(banner.getSubtitle()).isEqualTo("부제목");
        assertThat(banner.getPlacement()).isEqualTo(BannerPlacement.HOME_TOP);
        assertThat(banner.isActive()).isTrue();
        assertThat(banner.getDisplayOrder()).isEqualTo(1);
        assertThat(banner.getCreatedAt()).isNotNull();
        assertThat(banner.getUpdatedAt()).isNotNull();
    }

    @DisplayName("배너 수정 시 필드가 갱신된다")
    @Test
    void shouldUpdateBannerFields() {
        // given
        final Banner banner = createBanner();

        // when
        banner.update("새 제목", "새 부제목", "https://img.jpg", null, "/home", "#000000", "#FFFFFF", "bell", 2);

        // then
        assertThat(banner.getTitle()).isEqualTo("새 제목");
        assertThat(banner.getSubtitle()).isEqualTo("새 부제목");
        assertThat(banner.getImageUrl()).isEqualTo("https://img.jpg");
        assertThat(banner.getDisplayOrder()).isEqualTo(2);
    }

    @DisplayName("활성 배너가 5개 미만이면 활성화에 성공한다")
    @Test
    void shouldActivateWhenUnderLimit() {
        // given
        final Banner banner = createInactiveBanner();

        // when
        banner.activate(4);

        // then
        assertThat(banner.isActive()).isTrue();
    }

    @DisplayName("활성 배너가 5개이면 활성화 시 예외가 발생한다")
    @Test
    void shouldThrowWhenActivatingAtMaxLimit() {
        // given
        final Banner banner = createInactiveBanner();

        // when & then
        assertThatThrownBy(() -> banner.activate(5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("최대 5개");
    }

    @DisplayName("이미 활성인 배너를 활성화하면 멱등하게 무시된다")
    @Test
    void shouldBeIdempotentWhenAlreadyActive() {
        // given
        final Banner banner = createBanner();
        assertThat(banner.isActive()).isTrue();

        // when
        banner.activate(5);

        // then
        assertThat(banner.isActive()).isTrue();
    }

    @DisplayName("배너를 비활성화할 수 있다")
    @Test
    void shouldDeactivateBanner() {
        // given
        final Banner banner = createBanner();

        // when
        banner.deactivate();

        // then
        assertThat(banner.isActive()).isFalse();
    }

    @DisplayName("이미 비활성인 배너를 비활성화하면 멱등하게 무시된다")
    @Test
    void shouldBeIdempotentWhenAlreadyInactive() {
        // given
        final Banner banner = createInactiveBanner();

        // when
        banner.deactivate();

        // then
        assertThat(banner.isActive()).isFalse();
    }

    @DisplayName("배너 순서를 변경할 수 있다")
    @Test
    void shouldReorderBanner() {
        // given
        final Banner banner = createBanner();

        // when
        banner.reorder(5);

        // then
        assertThat(banner.getDisplayOrder()).isEqualTo(5);
    }

    @DisplayName("동일 ID의 배너는 equals가 true이다")
    @Test
    void shouldBeEqualById() {
        // given
        final Banner banner = createBanner();
        final Banner same = new Banner(
                banner.getId(), "다른 제목", "다른 부제목", BannerPlacement.HOME_TOP,
                null, null, null, null, null, null,
                0, false, banner.getCreatedAt(), banner.getUpdatedAt()
        );

        // then
        assertThat(banner).isEqualTo(same);
        assertThat(banner.hashCode()).isEqualTo(same.hashCode());
    }

    private Banner createBanner() {
        return Banner.init(
                "제목", "부제목", BannerPlacement.HOME_TOP,
                null, null, "/calendar",
                "#1F2937", "#FFFFFF", "calendar", 1
        );
    }

    private Banner createInactiveBanner() {
        final Banner banner = createBanner();
        banner.deactivate();
        return banner;
    }
}
