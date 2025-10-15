package com.official.lockr.domain.club.squard.domain.vo;

public class BackNumber {

    private final int value;

    public BackNumber(final int value) {
        verify(value);
        this.value = value;
    }

    private static void verify(final int value) {
        if (value < 0 || value >= 100) {
            throw new IllegalArgumentException();
        }
    }

    public int getValue() {
        return value;
    }
}
