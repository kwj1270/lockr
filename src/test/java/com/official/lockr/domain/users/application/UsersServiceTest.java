package com.official.lockr.domain.users.application;

import com.official.lockr.domain.users.application.command.WithdrawUsersCommand;
import com.official.lockr.domain.users.domain.ClubMembershipQuery;
import com.official.lockr.domain.users.domain.Users;
import com.official.lockr.domain.users.domain.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsersServiceTest {

    private UsersRepository usersRepository;
    private ClubMembershipQuery clubMembershipQuery;
    private UsersService usersService;

    @BeforeEach
    void setUp() {
        usersRepository = mock(UsersRepository.class);
        clubMembershipQuery = mock(ClubMembershipQuery.class);
        usersService = new UsersService(usersRepository, clubMembershipQuery);
    }

    @Test
    @DisplayName("존재하지 않는 회원은 탈퇴할 수 없다")
    void shouldThrowExceptionWhenUserNotFound() {
        // given
        String nonExistentUserId = "non-existent-user-id";
        WithdrawUsersCommand command = new WithdrawUsersCommand(nonExistentUserId);
        when(usersRepository.findById(nonExistentUserId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> usersService.withdraw(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }

    @Test
    @DisplayName("클럽에 가입된 회원은 탈퇴할 수 없다")
    void shouldThrowExceptionWhenUserHasClubMembership() {
        // given
        String userId = "user-with-club";
        Users user = Users.init();
        WithdrawUsersCommand command = new WithdrawUsersCommand(userId);

        when(usersRepository.findById(userId)).thenReturn(user);
        when(clubMembershipQuery.hasActiveClubMembership(userId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> usersService.withdraw(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("클럽에서 먼저 탈퇴해주세요.");
    }

    @Test
    @DisplayName("클럽에 가입되지 않은 회원은 탈퇴할 수 있다")
    void shouldWithdrawUserWhenNoClubMembership() {
        // given
        String userId = "user-without-club";
        Users user = Users.init();
        WithdrawUsersCommand command = new WithdrawUsersCommand(userId);

        when(usersRepository.findById(userId)).thenReturn(user);
        when(clubMembershipQuery.hasActiveClubMembership(userId)).thenReturn(false);

        // when
        usersService.withdraw(command);

        // then
        assertThat(user.isWithdrawn()).isTrue();
        verify(usersRepository).save(user);
    }
}
