package com.official.lockr.global.vo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public record BirthDate(
        String birthDate
) {
    public BirthDate {
        verify(birthDate);
    }

    private void verify(String birthDate) {
        if (birthDate == null || birthDate.isBlank()) {
            throw new IllegalArgumentException();
        }
        if (birthDate.length() != 8 || !birthDate.matches("\\d{8}")) {
            throw new IllegalArgumentException("BirthDate must be in yyyyMMdd format (got: " + birthDate + ")");
        }
        try {
            LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date: " + birthDate, e);
        }
    }
}
