package com.official.lockr.domain.users.api.dto;

import com.official.lockr.global.vo.Gender;

public record UserAdditionalInfoResponse(
        String id,
        String userId,
        String name,
        String birthDate,
        String phone,
        Gender gender,
        boolean isFilled
) {
    public static UserAdditionalInfoResponse of(
            String id,
            String userId,
            String name,
            String birthDate,
            String phone,
            Gender gender
    ) {
        boolean isFilled = name != null && birthDate != null && phone != null && gender != null;
        return new UserAdditionalInfoResponse(id, userId, name, birthDate, phone, gender, isFilled);
    }
}
