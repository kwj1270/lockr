package com.official.lockr.domain.club.sport.football.lineup.domain;

import java.util.List;

public interface LineupRepository {
    Lineup findById(String id);

    List<Lineup> findByClubId(String clubdId);

    Lineup save(final Lineup lineup);

    List<Lineup> saveAll(final List<Lineup> lineups);

    void delete(final Lineup lineup);

}

