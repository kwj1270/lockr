package com.official.lockr.domain.game.match.domain;

public interface Managers {
    Manager find(final String teamId, final String userId);
}
