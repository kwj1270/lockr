package com.official.lockr.global.vo;

public record BackNumber(int value) {

    public BackNumber {
        verify(value);
    }

    private static void verify(final int value) {
        if (value < 0 || value >= 100) {
            throw new IllegalArgumentException();
        }
    }
}
