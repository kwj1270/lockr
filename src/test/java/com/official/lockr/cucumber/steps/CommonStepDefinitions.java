package com.official.lockr.cucumber.steps;

import com.official.lockr.domain.users.domain.Users;
import io.cucumber.java.Before;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.먼저;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 여러 Feature에서 공통으로 사용되는 Step Definitions
 */
public class CommonStepDefinitions {

    private final SharedState state = SharedState.getInstance();

    @Before
    public void setUp() {
        state.clear();
    }

    @먼저("{string} 사용자가 존재한다")
    public void 사용자가_존재한다(String userId) {
        Users user = Users.init();
        state.getUsersMap().put(userId, user);
    }

    @먼저("{string}은 {string} 클럽의 멤버이다")
    @먼저("{string}이 {string} 클럽의 멤버이다")
    public void 은_클럽의_멤버이다(String userId, String clubName) {
        state.getUserClubMemberships().add(userId + ":" + clubName);
    }

    @그러면("{string} 오류가 발생한다")
    public void 오류가_발생한다(String errorMessage) {
        assertThat(state.getCaughtException()).isNotNull();
        assertThat(state.getLastErrorMessage()).isEqualTo(errorMessage);
    }

    @그러면("권한 없음 오류가 발생한다")
    public void 권한_없음_오류가_발생한다() {
        assertThat(state.getCaughtException()).isNotNull();
        assertThat(state.getCaughtException()).isInstanceOf(IllegalArgumentException.class);
    }
}
