package com.official.lockr.domain.club.sqaud.application;

import com.official.lockr.domain.club.sqaud.application.dto.AddSquadPlayerCommand;
import com.official.lockr.domain.club.sqaud.application.usecase.AddSquadPlayerUseCase;
import com.official.lockr.domain.club.sqaud.domain.squad.*;
import com.official.lockr.domain.club.common.BackNumber;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

import static com.github.f4b6a3.ulid.UlidCreator.getUlid;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class SquadService implements AddSquadPlayerUseCase {

    private final RecruitmentInfos recruitmentInfos;
    private final SquadRepository squadRepository;

    public SquadService(final RecruitmentInfos recruitmentInfos,
                        final SquadRepository squadRepository) {
        this.recruitmentInfos = recruitmentInfos;
        this.squadRepository = squadRepository;
    }

    @Override
    public Squad addSquadPlayer(final AddSquadPlayerCommand command) {
        final Squad squad = squad(command.clubId());
        if (squad.hasPlayer(command.memberId())) {
            return squad;
        }
        final BackNumber backNumber = new BackNumber(randomNumber());
        final RecruitmentInfo recruitmentInfo = recruitmentInfos.find(command.clubId(), command.userId());
        final SquadPlayer squadPlayer = squadMemer(squad.getId(), command.memberId(), recruitmentInfo, backNumber);
        squad.addPlayer(squadPlayer);
        return squadRepository.save(squad);
    }

    private Squad squad(final String clubId) {
        final Squad squad = squadRepository.findByClubId(clubId);
        if (nonNull(squad)) {
            return squad;
        }
        return new Squad(getUlid().toString(), clubId, new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now(), null);
    }

    private SquadPlayer squadMemer(final String squadId, String memberId, final RecruitmentInfo recruitmentInfo, final BackNumber backNumber) {
        if (isNull(recruitmentInfo)) {
            return SquadPlayer.init(getUlid().toString(), squadId, memberId, backNumber);
        }
        return SquadPlayer.init(getUlid().toString(), squadId, memberId, recruitmentInfo, backNumber);
    }

    private static int randomNumber() {
        return new Random().nextInt(99);
    }
}
