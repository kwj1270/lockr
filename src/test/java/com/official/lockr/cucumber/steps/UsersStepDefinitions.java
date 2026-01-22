package com.official.lockr.cucumber.steps;

import com.official.lockr.domain.users.domain.Users;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import io.cucumber.java.ko.먼저;

import static org.assertj.core.api.Assertions.assertThat;

public class UsersStepDefinitions {

    private final SharedState state = SharedState.getInstance();

    @먼저("{string}은 어떤 클럽에도 가입하지 않았다")
    @먼저("{string}이 어떤 클럽에도 가입하지 않았다")
    public void 은_어떤_클럽에도_가입하지_않았다(String userId) {
        state.getUserClubMemberships().removeIf(m -> m.startsWith(userId + ":"));
    }

    @만약("{string}이 회원 탈퇴를 요청한다")
    @만약("{string}가 회원 탈퇴를 요청한다")
    public void 이_회원_탈퇴를_요청한다(String userId) {
        Users user = state.getUsersMap().get(userId);
        if (user == null) {
            state.setCaughtException(new IllegalArgumentException("존재하지 않는 회원입니다"));
            state.setLastErrorMessage("존재하지 않는 회원입니다");
            return;
        }
        if (user.isWithdrawn()) {
            state.setCaughtException(new IllegalStateException("이미 탈퇴한 회원입니다"));
            state.setLastErrorMessage("이미 탈퇴한 회원입니다");
            return;
        }
        boolean hasClubMembership = state.getUserClubMemberships().stream()
                .anyMatch(m -> m.startsWith(userId + ":"));
        if (hasClubMembership) {
            state.setCaughtException(new IllegalStateException("클럽에서 먼저 탈퇴해주세요"));
            state.setLastErrorMessage("클럽에서 먼저 탈퇴해주세요");
            return;
        }
        user.withdraw();
    }

    @그러면("탈퇴가 성공적으로 처리된다")
    public void 탈퇴가_성공적으로_처리된다() {
        assertThat(state.getCaughtException()).isNull();
    }

    @그리고("{string}의 계정 상태가 {string}가 된다")
    @그리고("{string}의 계정 상태가 {string}이 된다")
    public void 의_계정_상태가_가_된다(String userId, String status) {
        Users user = state.getUsersMap().get(userId);
        if ("탈퇴".equals(status)) {
            assertThat(user.isWithdrawn()).isTrue();
        }
    }

    @먼저("{string}은 이미 탈퇴한 상태이다")
    @먼저("{string}이 이미 탈퇴한 상태이다")
    public void 은_이미_탈퇴한_상태이다(String userId) {
        Users user = state.getUsersMap().get(userId);
        if (user != null && !user.isWithdrawn()) {
            user.withdraw();
        }
    }
}
