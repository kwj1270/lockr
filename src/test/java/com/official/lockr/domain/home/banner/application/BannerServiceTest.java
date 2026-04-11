package com.official.lockr.domain.home.banner.application;

import com.official.lockr.domain.home.banner.application.command.CreateBannerCommand;
import com.official.lockr.domain.home.banner.application.command.DeleteBannerCommand;
import com.official.lockr.domain.home.banner.application.command.ReorderBannerCommand;
import com.official.lockr.domain.home.banner.application.command.ToggleBannerActiveCommand;
import com.official.lockr.domain.home.banner.application.command.UpdateBannerCommand;
import com.official.lockr.domain.home.banner.domain.Banner;
import com.official.lockr.domain.home.banner.domain.BannerPlacement;
import com.official.lockr.domain.home.banner.infrastructure.InMemoryBannerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BannerServiceTest {

    private InMemoryBannerRepository bannerRepository;
    private BannerService bannerService;

    @BeforeEach
    void setUp() {
        bannerRepository = new InMemoryBannerRepository();
        bannerService = new BannerService(bannerRepository);
    }

    @DisplayName("배너를 생성하면 저장소에 저장된다")
    @Test
    void shouldCreateBanner() {
        // given
        final CreateBannerCommand command = new CreateBannerCommand(
                "제목", "부제목", "HOME_TOP",
                null, null, "/calendar",
                "#1F2937", "#FFFFFF", "calendar", 1
        );

        // when
        final Banner banner = bannerService.create(command);

        // then
        assertThat(banner.getId()).isNotNull();
        assertThat(banner.getTitle()).isEqualTo("제목");
        assertThat(bannerRepository.findById(banner.getId())).isNotNull();
    }

    @DisplayName("배너를 수정하면 필드가 갱신된다")
    @Test
    void shouldUpdateBanner() {
        // given
        final Banner created = createTestBanner();
        final UpdateBannerCommand command = new UpdateBannerCommand(
                created.getId(), "새 제목", "새 부제목",
                "https://img.jpg", null, "/home",
                "#000000", "#FFFFFF", "bell", 2
        );

        // when
        bannerService.update(command);

        // then
        final Banner updated = bannerRepository.findById(created.getId());
        assertThat(updated.getTitle()).isEqualTo("새 제목");
        assertThat(updated.getDisplayOrder()).isEqualTo(2);
    }

    @DisplayName("존재하지 않는 배너 수정 시 예외가 발생한다")
    @Test
    void shouldThrowWhenUpdatingNonExistentBanner() {
        // given
        final UpdateBannerCommand command = new UpdateBannerCommand(
                "non-existent", "제목", "부제목",
                null, null, null, null, null, null, 1
        );

        // when & then
        assertThatThrownBy(() -> bannerService.update(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("배너를 찾을 수 없습니다");
    }

    @DisplayName("배너를 삭제하면 저장소에서 제거된다")
    @Test
    void shouldDeleteBanner() {
        // given
        final Banner created = createTestBanner();

        // when
        bannerService.delete(new DeleteBannerCommand(created.getId()));

        // then
        assertThat(bannerRepository.findById(created.getId())).isNull();
    }

    @DisplayName("존재하지 않는 배너 삭제 시 예외가 발생한다")
    @Test
    void shouldThrowWhenDeletingNonExistentBanner() {
        assertThatThrownBy(() -> bannerService.delete(new DeleteBannerCommand("non-existent")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("배너를 비활성화할 수 있다")
    @Test
    void shouldDeactivateBanner() {
        // given
        final Banner created = createTestBanner();

        // when
        bannerService.toggle(new ToggleBannerActiveCommand(created.getId(), false));

        // then
        final Banner deactivated = bannerRepository.findById(created.getId());
        assertThat(deactivated.isActive()).isFalse();
    }

    @DisplayName("비활성 배너를 활성화할 수 있다")
    @Test
    void shouldActivateBanner() {
        // given
        final Banner created = createTestBanner();
        bannerService.toggle(new ToggleBannerActiveCommand(created.getId(), false));

        // when
        bannerService.toggle(new ToggleBannerActiveCommand(created.getId(), true));

        // then
        final Banner activated = bannerRepository.findById(created.getId());
        assertThat(activated.isActive()).isTrue();
    }

    @DisplayName("활성 배너가 5개이면 추가 활성화 시 예외가 발생한다")
    @Test
    void shouldThrowWhenActivatingOverLimit() {
        // given — 5개 활성 배너 생성
        for (int i = 0; i < 5; i++) {
            createTestBannerWithOrder(i);
        }
        // 6번째 배너 생성 후 비활성화
        final Banner sixth = createTestBannerWithOrder(5);
        bannerService.toggle(new ToggleBannerActiveCommand(sixth.getId(), false));

        // when & then — 6번째 배너 활성화 시도
        assertThatThrownBy(() -> bannerService.toggle(
                new ToggleBannerActiveCommand(sixth.getId(), true)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("최대 5개");
    }

    @DisplayName("배너 순서를 변경할 수 있다")
    @Test
    void shouldReorderBanner() {
        // given
        final Banner created = createTestBanner();

        // when
        bannerService.reorder(new ReorderBannerCommand(created.getId(), 10));

        // then
        final Banner reordered = bannerRepository.findById(created.getId());
        assertThat(reordered.getDisplayOrder()).isEqualTo(10);
    }

    private Banner createTestBanner() {
        return createTestBannerWithOrder(1);
    }

    private Banner createTestBannerWithOrder(final int order) {
        return bannerService.create(new CreateBannerCommand(
                "제목", "부제목", "HOME_TOP",
                null, null, "/calendar",
                "#1F2937", "#FFFFFF", "calendar", order
        ));
    }
}
