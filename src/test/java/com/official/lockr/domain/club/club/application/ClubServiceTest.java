package com.official.lockr.domain.club.club.application;

import com.official.lockr.domain.club.club.application.command.AddMemberCommand;
import com.official.lockr.domain.club.club.application.command.FoundClubCommand;
import com.official.lockr.domain.club.club.application.command.UpdateMemberProfileImageCommand;
import com.official.lockr.domain.club.club.domain.Club;
import com.official.lockr.domain.club.club.domain.ClubRepository;
import com.official.lockr.domain.club.club.domain.Member;
import com.official.lockr.domain.users.domain.UserAdditionalInfo;
import com.official.lockr.domain.users.domain.Users;
import com.official.lockr.domain.users.domain.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClubServiceTest {

    private ClubRepository clubRepository;
    private UsersRepository usersRepository;
    private ClubService clubService;

    @BeforeEach
    void setUp() {
        clubRepository = mock(ClubRepository.class);
        usersRepository = mock(UsersRepository.class);
        clubService = new ClubService(clubRepository, usersRepository);
    }

    @Test
    @DisplayName("Club 창단 시 창단자의 UserAdditionalInfo.profileImage가 Member에 복사된다")
    void shouldCopyUserProfileImageToMemberWhenFoundingClub() {
        // given
        String userId = "user-001";
        String profileImage = "https://example.com/user-profile.jpg";

        LocalDateTime now = LocalDateTime.now();
        UserAdditionalInfo additionalInfo = new UserAdditionalInfo(
                "info-001", userId, "홍길동", null, null, null, profileImage, now, now, null
        );
        Users user = new Users(userId, additionalInfo, now, now, null);

        when(usersRepository.findById(userId)).thenReturn(user);
        when(clubRepository.findByName("FC 테스트")).thenReturn(null);
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FoundClubCommand command = new FoundClubCommand(
                userId, "FC 테스트", "FOOTBALL", "서울", "강남구", "테스트 클럽", null, null
        );

        // when
        Club club = clubService.found(command);

        // then
        assertThat(club.getMembers()).hasSize(1);
        Member president = club.getMembers().get(0);
        assertThat(president.isPresident()).isTrue();
        assertThat(president.getProfileImage()).isEqualTo(profileImage);
    }

    @Test
    @DisplayName("창단자의 프로필 이미지가 없으면 Member의 profileImage는 null이다")
    void shouldHaveNullProfileImageWhenUserHasNoProfileImage() {
        // given
        String userId = "user-001";

        LocalDateTime now = LocalDateTime.now();
        UserAdditionalInfo additionalInfo = new UserAdditionalInfo(
                "info-001", userId, "홍길동", null, null, null, null, now, now, null
        );
        Users user = new Users(userId, additionalInfo, now, now, null);

        when(usersRepository.findById(userId)).thenReturn(user);
        when(clubRepository.findByName("FC 테스트")).thenReturn(null);
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FoundClubCommand command = new FoundClubCommand(
                userId, "FC 테스트", "FOOTBALL", "서울", "강남구", "테스트 클럽", null, null
        );

        // when
        Club club = clubService.found(command);

        // then
        Member president = club.getMembers().get(0);
        assertThat(president.getProfileImage()).isNull();
    }

    @Test
    @DisplayName("입단 시 Command의 profileImage가 있으면 그것을 Member에 사용한다")
    void shouldUseCommandProfileImageWhenAddingMember() {
        // given
        String clubId = "club-001";
        String userId = "user-001";
        String newMemberUserId = "user-002";
        String applicationProfileImage = "https://example.com/application-profile.jpg";

        LocalDateTime now = LocalDateTime.now();
        Club club = new Club(
                clubId, userId, "FC 테스트", "FOOTBALL", "서울", "강남구", "테스트 클럽",
                null, null, new java.util.ArrayList<>(), now, now, null
        );
        club.addMember(Member.president(userId, clubId, null, null));

        when(clubRepository.findById(clubId)).thenReturn(club);
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddMemberCommand command = AddMemberCommand.basic(clubId, newMemberUserId, null, applicationProfileImage);

        // when
        Club result = clubService.addMember(command);

        // then
        assertThat(result.getMembers()).hasSize(2);
        Member newMember = result.getMembers().stream()
                .filter(m -> m.getUserId().equals(newMemberUserId))
                .findFirst().orElseThrow();
        assertThat(newMember.getProfileImage()).isEqualTo(applicationProfileImage);
    }

    @Test
    @DisplayName("입단 시 Command의 profileImage가 없으면 UserAdditionalInfo의 것을 사용한다")
    void shouldFallbackToUserProfileImageWhenCommandHasNoProfileImage() {
        // given
        String clubId = "club-001";
        String userId = "user-001";
        String newMemberUserId = "user-002";
        String userProfileImage = "https://example.com/user-profile.jpg";

        LocalDateTime now = LocalDateTime.now();
        Club club = new Club(
                clubId, userId, "FC 테스트", "FOOTBALL", "서울", "강남구", "테스트 클럽",
                null, null, new java.util.ArrayList<>(), now, now, null
        );
        club.addMember(Member.president(userId, clubId, null, null));

        UserAdditionalInfo additionalInfo = new UserAdditionalInfo(
                "info-002", newMemberUserId, "김철수", null, null, null, userProfileImage, now, now, null
        );
        Users newUser = new Users(newMemberUserId, additionalInfo, now, now, null);

        when(clubRepository.findById(clubId)).thenReturn(club);
        when(usersRepository.findById(newMemberUserId)).thenReturn(newUser);
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddMemberCommand command = AddMemberCommand.basic(clubId, newMemberUserId, null, null);

        // when
        Club result = clubService.addMember(command);

        // then
        Member newMember = result.getMembers().stream()
                .filter(m -> m.getUserId().equals(newMemberUserId))
                .findFirst().orElseThrow();
        assertThat(newMember.getProfileImage()).isEqualTo(userProfileImage);
    }

    @Test
    @DisplayName("본인의 Member 프로필 이미지를 변경할 수 있다")
    void shouldUpdateOwnMemberProfileImage() {
        // given
        String clubId = "club-001";
        String userId = "user-001";
        String memberId = "member-001";
        String newProfileImage = "https://example.com/new-profile.jpg";

        LocalDateTime now = LocalDateTime.now();
        Member member = new Member(memberId, userId, com.official.lockr.domain.club.club.domain.MemberRole.BASIC, clubId, null, "https://example.com/old.jpg", now, now, null);
        Club club = new Club(
                clubId, "founder-001", "FC 테스트", "FOOTBALL", "서울", "강남구", "테스트 클럽",
                null, null, new java.util.ArrayList<>(), now, now, null
        );
        club.addMember(member);

        when(clubRepository.findById(clubId)).thenReturn(club);
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateMemberProfileImageCommand command = new UpdateMemberProfileImageCommand(clubId, userId, newProfileImage);

        // when
        Club result = clubService.updateMemberProfileImage(command);

        // then
        Member updatedMember = result.getMembers().stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst().orElseThrow();
        assertThat(updatedMember.getProfileImage()).isEqualTo(newProfileImage);
    }

    @Test
    @DisplayName("클럽에 속하지 않은 사용자가 프로필 이미지 변경을 시도하면 예외 발생")
    void shouldThrowExceptionWhenNonMemberTriesToUpdateProfileImage() {
        // given
        String clubId = "club-001";
        String nonMemberUserId = "non-member";
        String newProfileImage = "https://example.com/new-profile.jpg";

        LocalDateTime now = LocalDateTime.now();
        Club club = new Club(
                clubId, "founder-001", "FC 테스트", "FOOTBALL", "서울", "강남구", "테스트 클럽",
                null, null, new java.util.ArrayList<>(), now, now, null
        );
        club.addMember(Member.president("founder-001", clubId, null, null));

        when(clubRepository.findById(clubId)).thenReturn(club);

        UpdateMemberProfileImageCommand command = new UpdateMemberProfileImageCommand(clubId, nonMemberUserId, newProfileImage);

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> clubService.updateMemberProfileImage(command))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
