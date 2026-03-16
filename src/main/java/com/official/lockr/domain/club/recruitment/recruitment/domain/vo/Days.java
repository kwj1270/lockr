package com.official.lockr.domain.club.recruitment.recruitment.domain.vo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Days {

    private final List<Day> days;

    public Days(final List<Day> days) {
        this.days = days;
    }

    public List<Day> getDays() {
        return days;
    }

    public static Days of(String... days) {
        return of(Arrays.stream(days).toList());
    }

    public static Days of(List<String> days) {
        return new Days(days.stream()
                .map(Day::valueOf)
                .toList());
    }

    public List<String> getDaysValue() {
        return days.stream()
                .map(Enum::name)
                .toList();
    }
}
