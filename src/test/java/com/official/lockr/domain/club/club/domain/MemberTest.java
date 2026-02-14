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
        Member member = Member.president("user-id", "club-id", "홍길동", profileImage);

        // then
        assertThat(member.getProfileImage()).isEqualTo(profileImage);
    }

    @Test
    @DisplayName("Member.president()가 name과 profileImage를 받는다")
    void shouldPresidentAcceptNameAndProfileImage() {
        // given
        String userId = "user-id";
        String clubId = "club-id";
        String name = "홍길동";
        String profileImage = "https://example.com/president.jpg";

        // when
        Member president = Member.president(userId, clubId, name, profileImage);

        // then
        assertThat(president.getRole()).isEqualTo(MemberRole.PRESIDENT);
        assertThat(president.getName()).isEqualTo(name);
        assertThat(president.getProfileImage()).isEqualTo(profileImage);
    }

    @Test
    @DisplayName("Member.basic()이 name과 profileImage를 받는다")
    void shouldBasicAcceptNameAndProfileImage() {
        // given
        String userId = "user-id";
        String clubId = "club-id";
        String name = "김철수";
        String profileImage = "https://example.com/member.jpg";

        // when
        Member basic = Member.basic(userId, clubId, name, profileImage);

        // then
        assertThat(basic.getRole()).isEqualTo(MemberRole.BASIC);
        assertThat(basic.getName()).isEqualTo(name);
        assertThat(basic.getProfileImage()).isEqualTo(profileImage);
    }

    @Test
    @DisplayName("Member.updateProfileImage()로 프로필 이미지를 변경할 수 있다")
    void shouldUpdateImage() {
        // given
        Member member = Member.basic("user-id", "club-id", "김철수", "https://example.com/old.jpg");
        String newProfileImage = "https://example.com/new.jpg";

        // when
        member.updateProfileImage(newProfileImage);

        // then
        assertThat(member.getProfileImage()).isEqualTo(newProfileImage);
    }

    @Test
    @DisplayName("Member.getName()으로 이름을 조회할 수 있다")
    void shouldGetName() {
        // given
        String name = "박영희";

        // when
        Member member = Member.basic("user-id", "club-id", name, null);

        // then
        assertThat(member.getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("Member.updateName()으로 이름을 변경할 수 있다")
    void shouldUpdateName() {
        // given
        Member member = Member.basic("user-id", "club-id", "이전이름", null);
        String newName = "새이름";

        // when
        member.updateName(newName);

        // then
        assertThat(member.getName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("name이 null인 Member를 생성할 수 있다")
    void shouldAllowNullName() {
        // when
        Member member = Member.basic("user-id", "club-id", null, null);

        // then
        assertThat(member.getName()).isNull();
    }
}
