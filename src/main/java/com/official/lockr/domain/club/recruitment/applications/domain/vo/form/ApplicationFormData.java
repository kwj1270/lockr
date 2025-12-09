package com.official.lockr.domain.club.recruitment.applications.domain.vo.form;

import java.time.LocalDate;

import static java.util.Objects.isNull;

public record ApplicationFormData(
        String name,
        String phone,
        String gender,
        String introduction,
        DetailedInfo detailedInfo
) {

    public boolean isDetailed() {
        return isNull(detailedInfo);
    }

    public String profileImageUrl() {
        return detailedInfo.profileImageUrl();
    }

    public LocalDate birthDate() {
        return detailedInfo.birthDate();
    }

    public record DetailedInfo(
            String profileImageUrl,
            String email,
            String address,
            LocalDate birthDate,
            String emergencyContactPhone
    ) {
    }
}

