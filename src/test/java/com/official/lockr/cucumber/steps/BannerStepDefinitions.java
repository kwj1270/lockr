package com.official.lockr.cucumber.steps;

import com.official.lockr.domain.home.banner.application.BannerService;
import com.official.lockr.domain.home.banner.application.command.CreateBannerCommand;
import com.official.lockr.domain.home.banner.application.command.DeleteBannerCommand;
import com.official.lockr.domain.home.banner.application.command.ReorderBannerCommand;
import com.official.lockr.domain.home.banner.application.command.ToggleBannerActiveCommand;
import com.official.lockr.domain.home.banner.application.command.UpdateBannerCommand;
import com.official.lockr.domain.home.banner.domain.Banner;
import com.official.lockr.domain.home.banner.domain.BannerPlacement;
import com.official.lockr.domain.home.banner.infrastructure.InMemoryBannerRepository;
import io.cucumber.java.Before;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import io.cucumber.java.ko.먼저;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BannerStepDefinitions {

    private final SharedState sharedState = SharedState.getInstance();
    private final InMemoryBannerRepository bannerRepository = new InMemoryBannerRepository();
    private final BannerService bannerService = new BannerService(bannerRepository);

    private Banner lastCreatedBanner;
    private Banner currentBanner;
    private List<Banner> queryResult;

    @Before
    public void setUp() {
        bannerRepository.clear();
        lastCreatedBanner = null;
        currentBanner = null;
        queryResult = null;
        sharedState.setCaughtException(null);
        sharedState.setLastErrorMessage(null);
    }

    // --- 배너 등록 ---

    @만약("{string}이 제목 {string}, 부제목 {string}, 위치 {string}으로 배너를 등록한다")
    public void 배너를_등록한다(String adminId, String title, String subtitle, String placement) {
        try {
            lastCreatedBanner = bannerService.create(new CreateBannerCommand(
                    title, subtitle, placement, null, null, null,
                    "#1F2937", "#FFFFFF", "calendar", 1
            ));
        } catch (Exception e) {
            sharedState.setCaughtException(e);
            sharedState.setLastErrorMessage(e.getMessage());
        }
    }

    @그러면("배너 등록이 성공한다")
    public void 배너_등록이_성공한다() {
        assertThat(lastCreatedBanner).isNotNull();
        assertThat(bannerRepository.findById(lastCreatedBanner.getId())).isNotNull();
    }

    @그리고("배너가 활성 상태이다")
    public void 배너가_활성_상태이다() {
        assertThat(lastCreatedBanner.isActive()).isTrue();
    }

    // --- 배너 수정 ---

    @먼저("{string} 위치에 배너 {string}이 존재한다")
    @먼저("{string} 위치에 배너 {string}가 존재한다")
    public void 위치에_배너가_존재한다(String placement, String title) {
        currentBanner = bannerService.create(new CreateBannerCommand(
                title, "부제목", placement, null, null, null,
                "#1F2937", "#FFFFFF", "calendar", 1
        ));
    }

    @만약("{string}이 배너 제목을 {string}으로 변경한다")
    @만약("{string}가 배너 제목을 {string}으로 변경한다")
    public void 배너_제목을_변경한다(String adminId, String newTitle) {
        try {
            bannerService.update(new UpdateBannerCommand(
                    currentBanner.getId(), newTitle, currentBanner.getSubtitle(),
                    null, null, null, null, null, null, currentBanner.getDisplayOrder()
            ));
        } catch (Exception e) {
            sharedState.setCaughtException(e);
            sharedState.setLastErrorMessage(e.getMessage());
        }
    }

    @그러면("배너 수정이 성공한다")
    public void 배너_수정이_성공한다() {
        assertThat(sharedState.getCaughtException()).isNull();
    }

    @만약("{string}이 존재하지 않는 배너를 수정하려고 한다")
    @만약("{string}가 존재하지 않는 배너를 수정하려고 한다")
    public void 존재하지_않는_배너를_수정하려고_한다(String adminId) {
        try {
            bannerService.update(new UpdateBannerCommand(
                    "non-existent", "제목", "부제목",
                    null, null, null, null, null, null, 1
            ));
        } catch (Exception e) {
            sharedState.setCaughtException(e);
            sharedState.setLastErrorMessage(e.getMessage());
        }
    }

    // --- 배너 삭제 ---

    @만약("{string}이 배너를 삭제한다")
    @만약("{string}가 배너를 삭제한다")
    public void 배너를_삭제한다(String adminId) {
        try {
            bannerService.delete(new DeleteBannerCommand(currentBanner.getId()));
        } catch (Exception e) {
            sharedState.setCaughtException(e);
            sharedState.setLastErrorMessage(e.getMessage());
        }
    }

    @그러면("배너 삭제가 성공한다")
    public void 배너_삭제가_성공한다() {
        assertThat(sharedState.getCaughtException()).isNull();
    }

    @그리고("해당 배너가 조회되지 않는다")
    public void 해당_배너가_조회되지_않는다() {
        assertThat(bannerRepository.findById(currentBanner.getId())).isNull();
    }

    // --- 활성/비활성 토글 ---

    @먼저("{string} 위치에 활성 배너 {string}가 존재한다")
    @먼저("{string} 위치에 활성 배너 {string}이 존재한다")
    public void 위치에_활성_배너가_존재한다(String placement, String title) {
        currentBanner = bannerService.create(new CreateBannerCommand(
                title, "부제목", placement, null, null, null,
                "#1F2937", "#FFFFFF", "calendar", 1
        ));
    }

    @만약("{string}이 배너를 비활성화한다")
    @만약("{string}가 배너를 비활성화한다")
    public void 배너를_비활성화한다(String adminId) {
        bannerService.toggle(new ToggleBannerActiveCommand(currentBanner.getId(), false));
    }

    @그러면("배너가 비활성 상태이다")
    public void 배너가_비활성_상태이다() {
        final Banner banner = bannerRepository.findById(currentBanner.getId());
        assertThat(banner.isActive()).isFalse();
    }

    @먼저("{string} 위치에 활성 배너가 {int}개 존재한다")
    public void 위치에_활성_배너가_N개_존재한다(String placement, int count) {
        for (int i = 0; i < count; i++) {
            bannerService.create(new CreateBannerCommand(
                    "배너" + i, "부제목", placement, null, null, null,
                    null, null, null, i + 1
            ));
        }
    }

    @먼저("{string} 위치에 비활성 배너 {string}가 존재한다")
    @먼저("{string} 위치에 비활성 배너 {string}이 존재한다")
    public void 위치에_비활성_배너가_존재한다(String placement, String title) {
        final Banner banner = bannerService.create(new CreateBannerCommand(
                title, "부제목", placement, null, null, null,
                null, null, null, 10
        ));
        bannerService.toggle(new ToggleBannerActiveCommand(banner.getId(), false));
        currentBanner = banner;
    }

    @만약("{string}이 {string} 배너를 활성화하려고 한다")
    @만약("{string}가 {string} 배너를 활성화하려고 한다")
    public void 배너를_활성화하려고_한다(String adminId, String title) {
        try {
            bannerService.toggle(new ToggleBannerActiveCommand(currentBanner.getId(), true));
        } catch (Exception e) {
            sharedState.setCaughtException(e);
            sharedState.setLastErrorMessage(e.getMessage());
        }
    }

    // --- 순서 변경 ---

    @먼저("{string} 위치에 배너 {string}이 순서 {int}로 존재한다")
    @먼저("{string} 위치에 배너 {string}가 순서 {int}로 존재한다")
    public void 위치에_배너가_순서로_존재한다(String placement, String title, int order) {
        currentBanner = bannerService.create(new CreateBannerCommand(
                title, "부제목", placement, null, null, null,
                null, null, null, order
        ));
    }

    @만약("{string}이 배너 순서를 {int}으로 변경한다")
    @만약("{string}가 배너 순서를 {int}으로 변경한다")
    @만약("{string}이 배너 순서를 {int}로 변경한다")
    @만약("{string}가 배너 순서를 {int}로 변경한다")
    public void 배너_순서를_변경한다(String adminId, int newOrder) {
        bannerService.reorder(new ReorderBannerCommand(currentBanner.getId(), newOrder));
    }

    @그러면("배너 순서가 {int}이다")
    public void 배너_순서가_이다(int expectedOrder) {
        final Banner banner = bannerRepository.findById(currentBanner.getId());
        assertThat(banner.getDisplayOrder()).isEqualTo(expectedOrder);
    }

    // --- 배너 조회 ---

    @먼저("{string} 위치에 활성 배너 {string}가 순서 {int}로 존재한다")
    @먼저("{string} 위치에 활성 배너 {string}이 순서 {int}로 존재한다")
    public void 위치에_활성_배너가_순서로_존재한다(String placement, String title, int order) {
        bannerService.create(new CreateBannerCommand(
                title, "부제목", placement, null, null, null,
                null, null, null, order
        ));
    }

    @만약("사용자가 {string} 위치의 배너를 조회한다")
    public void 사용자가_위치의_배너를_조회한다(String placement) {
        queryResult = new ArrayList<>();
        final BannerPlacement bp = BannerPlacement.valueOf(placement);
        // InMemoryRepository에서 활성 배너를 직접 필터링하여 조회 시뮬레이션
        bannerRepository.findAll().stream()
                .filter(b -> b.getPlacement() == bp && b.isActive())
                .sorted(Comparator.comparingInt(Banner::getDisplayOrder))
                .forEach(queryResult::add);
    }

    @그러면("{int}개의 배너가 반환된다")
    public void 개의_배너가_반환된다(int count) {
        assertThat(queryResult).hasSize(count);
    }

    @그리고("{string}가 {string}보다 먼저 반환된다")
    @그리고("{string}이 {string}보다 먼저 반환된다")
    public void 가_보다_먼저_반환된다(String first, String second) {
        int firstIdx = -1, secondIdx = -1;
        for (int i = 0; i < queryResult.size(); i++) {
            if (queryResult.get(i).getTitle().equals(first)) firstIdx = i;
            if (queryResult.get(i).getTitle().equals(second)) secondIdx = i;
        }
        assertThat(firstIdx).isLessThan(secondIdx);
    }

    @그러면("배너가 {int}개 반환된다")
    public void 배너가_N개_반환된다(int count) {
        assertThat(queryResult).hasSize(count);
    }
}
