package com.official.lockr.domain.users.domain;

import com.official.lockr.global.vo.BirthDate;
import com.official.lockr.global.vo.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserAdditionalInfoTest {

    @Test
    @DisplayName("UserAdditionalInfo에 profileImage 필드가 있어야 한다")
    void shouldHaveProfileImageField() {
        // given
        String id = "test-id";
        String userId = "user-id";
        String profileImage = "https://example.com/image.jpg";

        // when
        UserAdditionalInfo info = new UserAdditionalInfo(
                id, userId, "홍길동", new BirthDate("19900101"), "01012345678",
                Gender.MALE, profileImage, null, null, null
        );

        // then
        assertThat(info.getProfileImage()).isEqualTo(profileImage);
    }

    @Test
    @DisplayName("UserAdditionalInfo.update()에서 profileImage를 업데이트할 수 있다")
    void shouldUpdateProfileImage() {
        // given
        UserAdditionalInfo info = UserAdditionalInfo.init("test-id", "user-id");
        String newProfileImage = "https://example.com/new-image.jpg";

        // when
        info.update("홍길동", new BirthDate("19900101"), "01012345678", Gender.MALE, newProfileImage);

        // then
        assertThat(info.getProfileImage()).isEqualTo(newProfileImage);
    }

    @Test
    @DisplayName("UserAdditionalInfo.init()은 profileImage가 null인 상태로 생성된다")
    void shouldInitWithNullProfileImage() {
        // when
        UserAdditionalInfo info = UserAdditionalInfo.init("test-id", "user-id");

        // then
        assertThat(info.getProfileImage()).isNull();
    }
}
