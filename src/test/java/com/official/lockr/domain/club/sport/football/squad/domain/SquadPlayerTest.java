package com.official.lockr.domain.club.sport.football.squad.domain;

import com.official.lockr.global.vo.BackNumber;
import com.official.lockr.global.vo.Foot;
import com.official.lockr.global.vo.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SquadPlayerTest {

    @Test
    @DisplayName("SquadPlayer.init()은 스포츠 데이터 필드를 null로 생성한다 (backNumber 제외)")
    void shouldInitWithSportsDataFieldsAsNull() {
        // given
        String id = "player-001";
        String squadId = "squad-001";
        String userId = "user-001";
        BackNumber backNumber = new BackNumber(10);

        // when
        SquadPlayer player = SquadPlayer.init(id, squadId, userId, backNumber);

        // then
        assertThat(player.getId()).isEqualTo(id);
        assertThat(player.getSquadId()).isEqualTo(squadId);
        assertThat(player.getUserId()).isEqualTo(userId);
        assertThat(player.getBackNumber()).isEqualTo(backNumber);
        assertThat(player.getBirthDate()).isNull();
        assertThat(player.getHeight()).isNull();
        assertThat(player.getWeight()).isNull();
        assertThat(player.getFoot()).isNull();
        assertThat(player.getPositions()).isNull();
    }

    @Test
    @DisplayName("SquadPlayer.update()로 스포츠 데이터 필드를 변경할 수 있다")
    void shouldUpdateSportsDataFields() {
        // given
        SquadPlayer player = SquadPlayer.init("player-001", "squad-001", "user-001", new BackNumber(10));

        // when
        player.update("19950101", "180", "75", Foot.RIGHT, List.of(Position.FW, Position.MF), new BackNumber(7));

        // then
        assertThat(player.getBirthDate().birthDate()).isEqualTo("19950101");
        assertThat(player.getHeight()).isEqualTo("180");
        assertThat(player.getWeight()).isEqualTo("75");
        assertThat(player.getFoot()).isEqualTo(Foot.RIGHT);
        assertThat(player.getPositions()).containsExactly(Position.FW, Position.MF);
        assertThat(player.getBackNumber().value()).isEqualTo(7);
    }

    @Test
    @DisplayName("SquadPlayer.update()에 birthDate가 null이면 기존 birthDate를 유지한다")
    void shouldKeepExistingBirthDateWhenUpdateWithNull() {
        // given
        SquadPlayer player = SquadPlayer.init("player-001", "squad-001", "user-001", new BackNumber(10));

        // when
        player.update(null, "180", "75", Foot.LEFT, List.of(Position.GK), new BackNumber(1));

        // then
        assertThat(player.getBirthDate()).isNull();
        assertThat(player.getHeight()).isEqualTo("180");
        assertThat(player.getWeight()).isEqualTo("75");
        assertThat(player.getFoot()).isEqualTo(Foot.LEFT);
        assertThat(player.getPositions()).containsExactly(Position.GK);
        assertThat(player.getBackNumber().value()).isEqualTo(1);
    }
}
