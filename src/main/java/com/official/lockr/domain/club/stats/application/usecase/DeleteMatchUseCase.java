package com.official.lockr.domain.club.stats.application.usecase;

public interface DeleteMatchUseCase {

    void delete(String clubId, String recordId, String userId);
}
