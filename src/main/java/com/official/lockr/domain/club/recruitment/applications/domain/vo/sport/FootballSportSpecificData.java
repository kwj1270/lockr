package com.official.lockr.domain.club.recruitment.applications.domain.vo.sport;

import java.util.Map;

public record FootballSportSpecificData(
        String position,
        String foot,
        String height,
        String weight,
        String career
) implements SportSpecificData {
    public FootballSportSpecificData(final Map<String, String> stringObjectMap) {
        this(
                stringObjectMap.getOrDefault("position", ""),
                stringObjectMap.getOrDefault("foot", ""),
                stringObjectMap.getOrDefault("height", ""),
                stringObjectMap.getOrDefault("weight", ""),
                stringObjectMap.getOrDefault("career", "")
        );
    }
}
