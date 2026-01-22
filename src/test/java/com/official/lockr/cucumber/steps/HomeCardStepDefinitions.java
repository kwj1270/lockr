package com.official.lockr.cucumber.steps;

import io.cucumber.java.Before;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import io.cucumber.java.ko.먼저;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class HomeCardStepDefinitions {

    private final SharedState sharedState = SharedState.getInstance();
    private final Map<String, List<String>> userPinnedClubs = new HashMap<>();
    private final Map<String, LocalDateTime> clubNextSchedules = new HashMap<>();
    private List<HomeCard> lastQueryResult;
    private boolean pinSuccess;

    @Before
    public void setUp() {
        userPinnedClubs.clear();
        clubNextSchedules.clear();
        lastQueryResult = null;
        pinSuccess = false;
    }

    @먼저("{string}은 핀된 클럽이 없다")
    @먼저("{string}이 핀된 클럽이 없다")
    public void 은_핀된_클럽이_없다(String userId) {
        userPinnedClubs.remove(userId);
    }

    @만약("{string}이 홈 카드 목록을 조회한다")
    @만약("{string}가 홈 카드 목록을 조회한다")
    public void 이_홈_카드_목록을_조회한다(String userId) {
        List<String> pinnedClubs = userPinnedClubs.getOrDefault(userId, new ArrayList<>());
        lastQueryResult = new ArrayList<>();
        for (String clubName : pinnedClubs) {
            LocalDateTime nextSchedule = clubNextSchedules.get(clubName);
            lastQueryResult.add(new HomeCard(clubName, nextSchedule));
        }
    }

    @그러면("빈 배열이 반환된다")
    public void 빈_배열이_반환된다() {
        assertThat(lastQueryResult).isEmpty();
    }

    @만약("{string}이 {string} 클럽을 핀 설정한다")
    @만약("{string}가 {string} 클럽을 핀 설정한다")
    public void 이_클럽을_핀_설정한다(String userId, String clubName) {
        boolean isMember = sharedState.getUserClubMemberships().contains(userId + ":" + clubName);
        if (!isMember) {
            sharedState.setCaughtException(new IllegalArgumentException("가입한 클럽만 핀 설정할 수 있습니다"));
            sharedState.setLastErrorMessage("가입한 클럽만 핀 설정할 수 있습니다");
            pinSuccess = false;
            return;
        }

        List<String> pinned = userPinnedClubs.computeIfAbsent(userId, k -> new ArrayList<>());
        if (pinned.size() >= 2) {
            sharedState.setCaughtException(new IllegalArgumentException("최대 2개까지만 핀 설정할 수 있습니다"));
            sharedState.setLastErrorMessage("최대 2개까지만 핀 설정할 수 있습니다");
            pinSuccess = false;
            return;
        }

        pinned.add(clubName);
        pinSuccess = true;
    }

    @그러면("핀 설정이 성공한다")
    public void 핀_설정이_성공한다() {
        assertThat(pinSuccess).isTrue();
    }

    @그리고("홈 카드에 {string}이 표시된다")
    @그리고("홈 카드에 {string}가 표시된다")
    public void 홈_카드에_이_표시된다(String clubName) {
        assertThat(userPinnedClubs.values().stream()
                .flatMap(List::stream)
                .anyMatch(c -> c.equals(clubName))).isTrue();
    }

    @먼저("{string}이 {string}을 핀 설정했다")
    @먼저("{string}가 {string}을 핀 설정했다")
    public void 이_을_핀_설정했다(String userId, String clubName) {
        userPinnedClubs.computeIfAbsent(userId, k -> new ArrayList<>()).add(clubName);
    }

    @만약("{string}이 {string}을 추가로 핀 설정하려고 한다")
    @만약("{string}가 {string}을 추가로 핀 설정하려고 한다")
    public void 이_을_추가로_핀_설정하려고_한다(String userId, String clubName) {
        이_클럽을_핀_설정한다(userId, clubName);
    }

    @먼저("{string}은 {string} 클럽의 멤버가 아니다")
    @먼저("{string}이 {string} 클럽의 멤버가 아니다")
    public void 은_클럽의_멤버가_아니다(String userId, String clubName) {
        sharedState.getUserClubMemberships().remove(userId + ":" + clubName);
    }

    @만약("{string}이 {string} 클럽을 핀 설정하려고 한다")
    @만약("{string}가 {string} 클럽을 핀 설정하려고 한다")
    public void 이_클럽을_핀_설정하려고_한다(String userId, String clubName) {
        이_클럽을_핀_설정한다(userId, clubName);
    }

    @그리고("{string} 클럽에 다음 주 훈련 일정이 있다")
    public void 클럽에_다음_주_훈련_일정이_있다(String clubName) {
        clubNextSchedules.put(clubName, LocalDateTime.now().plusWeeks(1));
    }

    @그러면("{string} 카드에 다음 일정 날짜가 포함된다")
    public void 카드에_다음_일정_날짜가_포함된다(String clubName) {
        assertThat(lastQueryResult).isNotEmpty();
        HomeCard card = lastQueryResult.stream()
                .filter(c -> c.clubName.equals(clubName))
                .findFirst()
                .orElse(null);
        assertThat(card).isNotNull();
        assertThat(card.nextScheduleDate).isNotNull();
    }

    private static class HomeCard {
        final String clubName;
        final LocalDateTime nextScheduleDate;

        HomeCard(String clubName, LocalDateTime nextScheduleDate) {
            this.clubName = clubName;
            this.nextScheduleDate = nextScheduleDate;
        }
    }
}
