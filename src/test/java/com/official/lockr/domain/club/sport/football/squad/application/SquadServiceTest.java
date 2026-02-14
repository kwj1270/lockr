package com.official.lockr.domain.club.sport.football.squad.application;

import com.official.lockr.domain.club.recruitment.applications.domain.ApplicationRepository;
import com.official.lockr.domain.club.sport.football.squad.application.command.RegisterMySquadProfileCommand;
import com.official.lockr.domain.club.sport.football.squad.domain.Squad;
import com.official.lockr.domain.club.sport.football.squad.domain.SquadPlayer;
import com.official.lockr.domain.club.sport.football.squad.domain.SquadRepository;
import com.official.lockr.domain.users.domain.UsersRepository;
import com.official.lockr.global.vo.BackNumber;
import com.official.lockr.global.vo.Foot;
import com.official.lockr.global.vo.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SquadServiceTest {

    private UsersRepository usersRepository;
    private ApplicationRepository applicationRepository;
    private SquadRepository squadRepository;
    private SquadService squadService;

    @BeforeEach
    void setUp() {
        usersRepository = mock(UsersRepository.class);
        applicationRepository = mock(ApplicationRepository.class);
        squadRepository = mock(SquadRepository.class);
        squadService = new SquadService(usersRepository, applicationRepository, squadRepository);
    }

    @Test
    @DisplayName("registerMyProfile은 스쿼드 플레이어의 스포츠 데이터를 업데이트한다")
    void shouldUpdateSquadPlayerSportsData() {
        // given
        String clubId = "club-001";
        String userId = "user-001";
        LocalDateTime now = LocalDateTime.now();

        List<SquadPlayer> players = new ArrayList<>();
        players.add(SquadPlayer.init("player-001", "squad-001", userId, new BackNumber(10)));

        Squad squad = new Squad("squad-001", clubId, players, now, now, null);

        when(squadRepository.findByClubId(clubId)).thenReturn(squad);
        when(squadRepository.save(any(Squad.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterMySquadProfileCommand command = new RegisterMySquadProfileCommand(
                userId, clubId, "180", "75", Foot.RIGHT, List.of(Position.FW, Position.MF), 7
        );

        // when
        Squad result = squadService.registerMyProfile(command);

        // then
        SquadPlayer updatedPlayer = result.findByUserId(userId);
        assertThat(updatedPlayer).isNotNull();
        assertThat(updatedPlayer.getHeight()).isEqualTo("180");
        assertThat(updatedPlayer.getWeight()).isEqualTo("75");
        assertThat(updatedPlayer.getFoot()).isEqualTo(Foot.RIGHT);
        assertThat(updatedPlayer.getPositions()).containsExactly(Position.FW, Position.MF);
        assertThat(updatedPlayer.getBackNumber().value()).isEqualTo(7);
    }

    @Test
    @DisplayName("registerMyProfile은 스쿼드에 없는 사용자가 요청하면 예외를 발생시킨다")
    void shouldThrowWhenUserNotInSquad() {
        // given
        String clubId = "club-001";
        String nonMemberUserId = "non-member";
        LocalDateTime now = LocalDateTime.now();

        List<SquadPlayer> players = new ArrayList<>();
        players.add(SquadPlayer.init("player-001", "squad-001", "user-001", new BackNumber(10)));

        Squad squad = new Squad("squad-001", clubId, players, now, now, null);

        when(squadRepository.findByClubId(clubId)).thenReturn(squad);

        RegisterMySquadProfileCommand command = new RegisterMySquadProfileCommand(
                nonMemberUserId, clubId, "180", "75", Foot.RIGHT, List.of(Position.FW), 7
        );

        // when & then
        assertThatThrownBy(() -> squadService.registerMyProfile(command))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
