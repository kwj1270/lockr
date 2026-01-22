package com.official.lockr.domain.users.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsersTest {

    @Test
    @DisplayName("회원은 탈퇴할 수 있다")
    void shouldWithdrawUser() {
        // given
        Users user = Users.init();

        // when
        user.withdraw();

        // then
        assertThat(user.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 탈퇴한 회원은 다시 탈퇴할 수 없다")
    void shouldThrowExceptionWhenAlreadyWithdrawn() {
        // given
        Users user = Users.init();
        user.withdraw();

        // when & then
        assertThatThrownBy(user::withdraw)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 탈퇴한 회원입니다.");
    }

    @Test
    @DisplayName("회원이 탈퇴 상태인지 확인할 수 있다")
    void shouldCheckIfUserIsWithdrawn() {
        // given
        Users user = Users.init();

        // when & then
        assertThat(user.isWithdrawn()).isFalse();

        user.withdraw();
        assertThat(user.isWithdrawn()).isTrue();
    }
}
