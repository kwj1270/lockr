package com.official.lockr.global.util;

import com.github.f4b6a3.ulid.UlidCreator;

import java.util.UUID;

public class UuidUtils {

    private UuidUtils() {
    }

    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }
}
