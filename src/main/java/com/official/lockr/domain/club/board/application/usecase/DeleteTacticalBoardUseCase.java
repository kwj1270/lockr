package com.official.lockr.domain.club.board.application.usecase;

public interface DeleteTacticalBoardUseCase {
    void delete(final String clubId, final String tacticalBoardId, final String coachUserId);
}
