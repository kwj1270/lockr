package com.official.lockr.domain.club.sport.football.lineup.domain;

import com.official.lockr.domain.club.sport.football.lineup.domain.vo.SlotType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LineupTest {

    @Test
    void shouldRemovePlayerFromSlotWhenSlotHasPlayer() {
        // given
        final Lineup lineup = new Lineup("club-001", "포메이션A");
        lineup.assignSlot("squad-player-001", "starter", 5);

        assertThat(lineup.getLineupPlayers()).hasSize(1);

        // when
        lineup.removeSlot("starter", 5);

        // then
        assertThat(lineup.getLineupPlayers()).isEmpty();
    }

    @Test
    void shouldDoNothingWhenRemovingFromEmptySlot() {
        // given
        final Lineup lineup = new Lineup("club-001", "포메이션A");

        assertThat(lineup.getLineupPlayers()).isEmpty();

        // when
        lineup.removeSlot("starter", 5);

        // then
        assertThat(lineup.getLineupPlayers()).isEmpty();
    }

    @Test
    void shouldOnlyRemoveTargetSlotWhenMultiplePlayersExist() {
        // given
        final Lineup lineup = new Lineup("club-001", "포메이션A");
        lineup.assignSlot("squad-player-001", "starter", 5);
        lineup.assignSlot("squad-player-002", "starter", 6);
        lineup.assignSlot("squad-player-003", "substitute", 0);

        assertThat(lineup.getLineupPlayers()).hasSize(3);

        // when
        lineup.removeSlot("starter", 5);

        // then
        assertThat(lineup.getLineupPlayers()).hasSize(2);
        assertThat(lineup.getLineupPlayers())
                .noneMatch(slot -> slot.isSameSlot(SlotType.STARTER, 5));
        assertThat(lineup.getLineupPlayers())
                .anyMatch(slot -> slot.isSameSlot(SlotType.STARTER, 6));
        assertThat(lineup.getLineupPlayers())
                .anyMatch(slot -> slot.isSameSlot(SlotType.SUBSTITUTE, 0));
    }

    @Test
    void shouldRemoveSubstituteSlotCorrectly() {
        // given
        final Lineup lineup = new Lineup("club-001", "포메이션A");
        lineup.assignSlot("squad-player-001", "substitute", 2);

        assertThat(lineup.getLineupPlayers()).hasSize(1);

        // when
        lineup.removeSlot("substitute", 2);

        // then
        assertThat(lineup.getLineupPlayers()).isEmpty();
    }
}
