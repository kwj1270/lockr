package com.official.lockr.domain.club.club.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClubTest {

    private Club clubWithMembers(Member... members) {
        ArrayList<Member> memberList = new ArrayList<>();
        for (Member m : members) {
            memberList.add(m);
        }
        return new Club(
                "club-001", "founder-001", "FC 테스트", "FOOT_BALL",
                "서울", "강남구", "설명", null, null,
                memberList, LocalDateTime.now(), LocalDateTime.now(), null
        );
    }

    @Test
    @DisplayName("BASIC 멤버는 클럽을 탈퇴할 수 있다")
    void shouldAllowBasicMemberToLeave() {
        // given
        Member basic = Member.basic("user-basic", "club-001", "홍길동", null);
        Club club = clubWithMembers(
                Member.president("user-president", "club-001", "회장", null),
                basic
        );

        // when
        club.removeMember("user-basic");

        // then
        assertThat(club.getMembers()).hasSize(1);
        assertThat(club.hasNotMember("user-basic")).isTrue();
    }

    @Test
    @DisplayName("PRESIDENT(운영진)는 탈퇴할 수 없다")
    void shouldThrowWhenPresidentTriesToLeave() {
        // given
        Club club = clubWithMembers(
                Member.president("user-president", "club-001", "회장", null)
        );

        // when & then
        assertThatThrownBy(() -> club.removeMember("user-president"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("운영진은 탈퇴할 수 없습니다. 먼저 역할을 해제해주세요.");
    }

    @Test
    @DisplayName("MANAGER(운영진)는 탈퇴할 수 없다")
    void shouldThrowWhenManagerTriesToLeave() {
        // given
        Member manager = new Member(
                "m-001", "user-manager", MemberRole.MANAGER, "club-001",
                "매니저", null, LocalDateTime.now(), LocalDateTime.now(), null
        );
        Club club = clubWithMembers(
                Member.president("user-president", "club-001", "회장", null),
                manager
        );

        // when & then
        assertThatThrownBy(() -> club.removeMember("user-manager"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("운영진은 탈퇴할 수 없습니다. 먼저 역할을 해제해주세요.");
    }

    @Test
    @DisplayName("회장(운영진)은 클럽 정보를 수정할 수 있다")
    void shouldUpdateClubInfoByPresidency() {
        // given
        Club club = clubWithMembers(
                Member.president("user-president", "club-001", "회장", null),
                Member.basic("user-basic", "club-001", "일반", null)
        );

        // when
        club.updateInfo("user-president", "새 클럽명", "새 설명", "부산", "해운대구", "img1.jpg", "bg1.jpg");

        // then
        assertThat(club.getName()).isEqualTo("새 클럽명");
        assertThat(club.getDescription()).isEqualTo("새 설명");
        assertThat(club.getCity()).isEqualTo("부산");
        assertThat(club.getDistrict()).isEqualTo("해운대구");
        assertThat(club.getProfileImageUrl()).isEqualTo("img1.jpg");
        assertThat(club.getBackgroundImageUrl()).isEqualTo("bg1.jpg");
    }

    @Test
    @DisplayName("운영진이 아닌 멤버가 클럽 정보 수정 시 예외 발생")
    void shouldThrowWhenNonPresidencyTriesToUpdateInfo() {
        // given
        Club club = clubWithMembers(
                Member.president("user-president", "club-001", "회장", null),
                Member.basic("user-basic", "club-001", "일반", null)
        );

        // when & then
        assertThatThrownBy(() -> club.updateInfo("user-basic", "새 클럽명", "새 설명", "부산", "해운대구", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("COACH(운영진)는 탈퇴할 수 없다")
    void shouldThrowWhenCoachTriesToLeave() {
        // given
        Member coach = new Member(
                "m-002", "user-coach", MemberRole.COACH, "club-001",
                "코치", null, LocalDateTime.now(), LocalDateTime.now(), null
        );
        Club club = clubWithMembers(
                Member.president("user-president", "club-001", "회장", null),
                coach
        );

        // when & then
        assertThatThrownBy(() -> club.removeMember("user-coach"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("운영진은 탈퇴할 수 없습니다. 먼저 역할을 해제해주세요.");
    }

}
