package com.official.lockr.global.vo;

public enum Gender {
    MALE("M"),
    FEMALE("F");

    private final String dbValue;

    Gender(String dbValue) {
        this.dbValue = dbValue;
    }

    public String toDbValue() {
        return dbValue;
    }

    public static Gender fromDbValue(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        return switch (dbValue.toUpperCase()) {
            case "M" -> MALE;
            case "F" -> FEMALE;
            default -> throw new IllegalArgumentException("Unknown gender DB value: " + dbValue);
        };
    }

    public static Gender fromString(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        return switch (normalized) {
            case "MALE", "M", "남", "남성" -> MALE;
            case "FEMALE", "F", "여", "여성" -> FEMALE;
            default -> throw new IllegalArgumentException("Unknown gender value: " + value);
        };
    }
}
