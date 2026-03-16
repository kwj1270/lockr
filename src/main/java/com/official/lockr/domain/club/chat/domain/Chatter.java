package com.official.lockr.domain.club.chat.domain;

import java.util.Objects;

public class Chatter {

    private final String userId;

    public Chatter(final String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isSame(final String userId) {
        return this.userId.equals(userId);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Chatter chatter = (Chatter) o;
        return Objects.equals(userId, chatter.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }
}
