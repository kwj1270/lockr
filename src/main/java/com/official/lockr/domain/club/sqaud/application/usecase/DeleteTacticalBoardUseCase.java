package com.official.lockr.domain.club.sqaud.application.usecase;

public interface DeleteTacticalBoardUseCase {
    void delete(final String squadId, final String coachUserId, final String tacticalBoardId);
}
