package com.official.lockr.domain.game.match.infrastructure;

import com.official.lockr.domain.game.match.domain.Manager;
import com.official.lockr.domain.game.match.domain.Managers;
import org.springframework.stereotype.Component;

@Component
public class JOOQManagerRepository implements Managers {
    @Override
    public Manager find(final String teamId, final String userId) {
        return null;
    }
}
