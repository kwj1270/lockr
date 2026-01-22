package com.official.lockr.domain.club.club.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    @Test
    @DisplayName("Member에 profileImage 필드가 있어야 한다")
    void shouldHaveProfileImageField() {
        // given
        String profileImage = "https://example.com/image.jpg";

        // when
        Member member = Member.president("user-id", "club-id", profileImage);

        // then
        assertThat(member.getProfileImage()).isEqualTo(profileImage);
    }

    @Test
    @DisplayName("Member.president()가 profileImage를 받는다")
    void shouldPresidentAcceptProfileImage() {
        // given
        String userId = "user-id";
        String clubId = "club-id";
        String profileImage = "https://example.com/president.jpg";

        // when
        Member president = Member.president(userId, clubId, profileImage);

        // then
        assertThat(president.getRole()).isEqualTo(MemberRole.PRESIDENT);
        assertThat(president.getProfileImage()).isEqualTo(profileImage);
    }

    @Test
    @DisplayName("Member.basic()이 profileImage를 받는다")
    void shouldBasicAcceptProfileImage() {
        // given
        String userId = "user-id";
        String clubId = "club-id";
        String profileImage = "https://example.com/member.jpg";

        // when
        Member basic = Member.basic(userId, clubId, profileImage);

        // then
        assertThat(basic.getRole()).isEqualTo(MemberRole.BASIC);
        assertThat(basic.getProfileImage()).isEqualTo(profileImage);
    }

    @Test
    @DisplayName("Member.updateProfileImage()로 프로필 이미지를 변경할 수 있다")
    void shouldUpdateProfileImage() {
        // given
        Member member = Member.basic("user-id", "club-id", "https://example.com/old.jpg");
        String newProfileImage = "https://example.com/new.jpg";

        // when
        member.updateProfileImage(newProfileImage);

        // then
        assertThat(member.getProfileImage()).isEqualTo(newProfileImage);
    }
}
