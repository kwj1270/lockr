package com.official.lockr.domain.users.api.dto;

import com.official.lockr.domain.users.application.command.UpdateUserAdditionalInfoCommand;
import com.official.lockr.global.vo.BirthDate;
import com.official.lockr.global.vo.Gender;

public record UserAdditionalInfoRequest(
        String name,
        String birthDate,
        String phone,
        String gender,
        String profileImage
) {
    public UpdateUserAdditionalInfoCommand toCommand(final String userId) {
        return new UpdateUserAdditionalInfoCommand(
                userId,
                name,
                new BirthDate(birthDate),
                phone,
                Gender.fromString(gender),
                profileImage
        );
    }
}
