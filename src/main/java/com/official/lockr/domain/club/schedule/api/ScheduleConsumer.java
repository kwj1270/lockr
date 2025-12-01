//package com.official.lockr.domain.club.schedule.api;
//
//import com.official.lockr.domain.club.schedule.application.dto.CreateScheduleCommand;
//import com.official.lockr.domain.club.schedule.application.usecase.RegisterScheduleUseCase;
//import com.official.lockr.domain.club.schedule.domain.ScheduleType;
//import com.official.lockr.domain.club.schedule.domain.vo.MatchDetailData;
//import com.official.lockr.domain.club.z_match.domain.event.AcceptedMatchEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//
//@Component
//public class ScheduleConsumer {
//
//    private RegisterScheduleUseCase registerScheduleUseCase;
//
//    public ScheduleConsumer(final RegisterScheduleUseCase registerScheduleUseCase) {
//        this.registerScheduleUseCase = registerScheduleUseCase;
//    }
//
//    @EventListener
//    public void homeConsume(final AcceptedMatchEvent acceptedMatchEvent) {
//        final CreateScheduleCommand createHomeScheduleCommand = home(acceptedMatchEvent);
//        registerScheduleUseCase.create(createHomeScheduleCommand);
//    }
//
//    @EventListener
//    public void awayConsume(final AcceptedMatchEvent acceptedMatchEvent) {
//        final CreateScheduleCommand createAwayScheduleCommand = away(acceptedMatchEvent);
//        registerScheduleUseCase.create(createAwayScheduleCommand);
//    }
//
//    private static CreateScheduleCommand home(final AcceptedMatchEvent acceptedMatchEvent) {
//        return new CreateScheduleCommand(
//                acceptedMatchEvent.homeClubMangerUserId(),
//                acceptedMatchEvent.homeClubId(),
//                "매칭 성공",
//                "경기가 매칭되었습니다.",
//                acceptedMatchEvent.location(),
//                acceptedMatchEvent.matchDateTime(),
//                ScheduleType.MATCH,
//                new MatchDetailData(acceptedMatchEvent.homeClubId(), acceptedMatchEvent.awayClubId()),
//                0, 100, 0
//        );
//    }
//
//    private static CreateScheduleCommand away(final AcceptedMatchEvent acceptedMatchEvent) {
//        return new CreateScheduleCommand(
//                acceptedMatchEvent.awayClubMangerUserId(),
//                acceptedMatchEvent.awayClubId(),
//                "매칭 성공",
//                "경기가 매칭되었습니다.",
//                acceptedMatchEvent.location(),
//                acceptedMatchEvent.matchDateTime(),
//                ScheduleType.MATCH,
//                new MatchDetailData(acceptedMatchEvent.homeClubId(), acceptedMatchEvent.awayClubId()),
//                0, 100, 0
//        );
//    }
//}
