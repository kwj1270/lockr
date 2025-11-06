package com.official.lockr.domain.club.schedule.api;

import com.official.lockr.domain.club.match.domain.event.AcceptedMatchEvent;
import com.official.lockr.domain.club.schedule.application.dto.CreateScheduleCommand;
import com.official.lockr.domain.club.schedule.application.usecase.CreateScheduleUseCase;
import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.official.lockr.domain.club.schedule.domain.detail.MatchDetail;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ScheduleConsumer {

    private CreateScheduleUseCase createScheduleUseCase;

    public ScheduleConsumer(final CreateScheduleUseCase createScheduleUseCase) {
        this.createScheduleUseCase = createScheduleUseCase;
    }

    @EventListener
    public void homeConsume(final AcceptedMatchEvent acceptedMatchEvent) {
        final CreateScheduleCommand createHomeScheduleCommand = home(acceptedMatchEvent);
        System.out.println("1");
        createScheduleUseCase.create(createHomeScheduleCommand);
    }

    @EventListener
    public void awayConsume(final AcceptedMatchEvent acceptedMatchEvent) {
        final CreateScheduleCommand createAwayScheduleCommand = away(acceptedMatchEvent);
        System.out.println("2");
        createScheduleUseCase.create(createAwayScheduleCommand);
    }

    private static CreateScheduleCommand home(final AcceptedMatchEvent acceptedMatchEvent) {
        return new CreateScheduleCommand(
                acceptedMatchEvent.homeClubMangerUserId(),
                acceptedMatchEvent.homeClubId(),
                "매칭 성공",
                "경기가 매칭되었습니다.",
                acceptedMatchEvent.location(),
                acceptedMatchEvent.matchDateTime(),
                ScheduleType.MATCH,
                new MatchDetail(acceptedMatchEvent.homeClubId(), acceptedMatchEvent.awayClubId())
        );
    }

    private static CreateScheduleCommand away(final AcceptedMatchEvent acceptedMatchEvent) {
        return new CreateScheduleCommand(
                acceptedMatchEvent.awayClubMangerUserId(),
                acceptedMatchEvent.awayClubId(),
                "매칭 성공",
                "경기가 매칭되었습니다.",
                acceptedMatchEvent.location(),
                acceptedMatchEvent.matchDateTime(),
                ScheduleType.MATCH,
                new MatchDetail(acceptedMatchEvent.homeClubId(), acceptedMatchEvent.awayClubId())
        );
    }
}
