package com.official.lockr.domain.auth.admin.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminTest {

    @Test
    void shouldHaveBasicRoleWhenCreatedWithInit() {
        // given
        final String id = "admin1";
        final String userId = "user123";
        final String password = "password123";

        // when
        final Admin admin = Admin.init(id, userId, password);

        // then
        assertThat(admin.getRole()).isEqualTo("BASIC");
    }

    @Test
    void shouldHaveUserIdWhenCreatedWithInit() {
        // given
        final String id = "admin1";
        final String userId = "user123";
        final String password = "password123";

        // when
        final Admin admin = Admin.init(id, userId, password);

        // then
        assertThat(admin.getUserId()).isEqualTo(userId);
    }

    @Test
    void shouldReturnTrueWhenUserIdIsNull() {
        // given
        final Admin admin = Admin.init("admin1", null, "password123");

        // when
        final boolean result = admin.hasNotUserId();

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserIdExists() {
        // given
        final Admin admin = Admin.init("admin1", "user123", "password123");

        // when
        final boolean result = admin.hasNotUserId();

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldBeEqualWhenSameId() {
        // given
        final Admin admin1 = Admin.init("admin1", "user123", "password123");
        final Admin admin2 = Admin.init("admin1", "user456", "differentPassword");

        // when & then
        assertThat(admin1).isEqualTo(admin2);
        assertThat(admin1.hashCode()).isEqualTo(admin2.hashCode());
    }

    @Test
    void shouldReturnTrueWhenPasswordMatches() {
        // given
        final Admin admin = Admin.init("admin1", "user123", "password123");

        // when
        final boolean result = admin.matchPassword("password123");

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenPasswordDoesNotMatch() {
        // given
        final Admin admin = Admin.init("admin1", "user123", "password123");

        // when
        final boolean result = admin.matchPassword("wrongPassword");

        // then
        assertThat(result).isFalse();
    }
}
