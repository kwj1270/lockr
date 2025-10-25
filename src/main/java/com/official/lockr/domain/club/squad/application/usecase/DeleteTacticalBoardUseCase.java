package com.official.lockr.domain.club.squad.application.usecase;

public interface DeleteTacticalBoardUseCase {
    void delete(final String squadId, final String coachUserId, final String tacticalBoardId);
}
