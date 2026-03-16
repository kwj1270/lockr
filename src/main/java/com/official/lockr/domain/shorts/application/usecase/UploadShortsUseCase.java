package com.official.lockr.domain.shorts.application.usecase;

import com.official.lockr.domain.shorts.application.command.UploadShortsCommand;
import com.official.lockr.domain.shorts.domain.Shorts;

public interface UploadShortsUseCase {

    Shorts upload(UploadShortsCommand command);
}
