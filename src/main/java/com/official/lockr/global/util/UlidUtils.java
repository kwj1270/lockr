package com.official.lockr.global.util;

import com.github.f4b6a3.ulid.UlidCreator;

public class UlidUtils {

    private UlidUtils() {
    }

    public static String generateUlid() {
        return UlidCreator.getUlid().toString();
    }
}
