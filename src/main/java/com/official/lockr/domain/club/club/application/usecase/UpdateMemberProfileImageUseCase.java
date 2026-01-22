package com.official.lockr.domain.club.club.application.usecase;

import com.official.lockr.domain.club.club.application.command.UpdateMemberProfileImageCommand;
import com.official.lockr.domain.club.club.domain.Club;

public interface UpdateMemberProfileImageUseCase {
    Club updateMemberProfileImage(UpdateMemberProfileImageCommand command);
}
