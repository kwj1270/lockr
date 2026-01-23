package com.official.lockr.domain.users.application.command;

import com.official.lockr.global.vo.BirthDate;
import com.official.lockr.global.vo.Gender;

public record UpdateUserAdditionalInfoCommand(
        String userId,
        String name,
        BirthDate birthDate,
        String phone,
        Gender gender,
        String profileImage
) {
}
