package com.official.lockr.domain.club.schedule.application;

import com.official.lockr.domain.club.club.domain.Club;
import com.official.lockr.domain.club.club.domain.ClubRepository;
import com.official.lockr.domain.club.club.domain.Member;
import com.official.lockr.domain.club.schedule.application.dto.CreateScheduleCommand;
import com.official.lockr.domain.club.schedule.application.dto.RespondToScheduleCommand;
import com.official.lockr.domain.club.schedule.application.dto.UpdateScheduleCommand;
import com.official.lockr.domain.club.schedule.application.usecase.CancelScheduleUseCase;
import com.official.lockr.domain.club.schedule.application.usecase.RegisterScheduleUseCase;
import com.official.lockr.domain.club.schedule.application.usecase.RespondToScheduleUseCase;
import com.official.lockr.domain.club.schedule.application.usecase.UpdateScheduleUseCase;
import com.official.lockr.domain.club.schedule.domain.Schedule;
import com.official.lockr.domain.club.schedule.domain.ScheduleRepository;
import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.official.lockr.domain.club.schedule.domain.vo.MatchDetailData;
import com.official.lockr.domain.notification.application.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class ScheduleService implements RegisterScheduleUseCase, CancelScheduleUseCase, RespondToScheduleUseCase, UpdateScheduleUseCase {

    private final ClubRepository clubRepository;
    private final ScheduleRepository scheduleRepository;
    private final NotificationService notificationService;

    public ScheduleService(
            final ClubRepository clubRepository,
            final ScheduleRepository scheduleRepository,
            final NotificationService notificationService
    ) {
        this.clubRepository = clubRepository;
        this.scheduleRepository = scheduleRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Schedule create(final CreateScheduleCommand command) {
        verifyStaff(command.userId(), command.clubId());
        final Schedule schedule = Schedule.create(
                generateUlid(), command.clubId(), command.title(), command.content(), command.location(),
                command.scheduleTime(), command.scheduleType(), command.detail(), attendanceUserIds(command.clubId()),
                command.minParticipants(), command.maxParticipants(), command.deadlineDays()
        );
        final Schedule savedSchedule = scheduleRepository.save(schedule);

        // 경기 일정인 경우, 상대팀이 등록된 클럽이면 알림 전송
        if (command.scheduleType() == ScheduleType.MATCH && command.detail() instanceof MatchDetailData matchDetail) {
            sendNotificationToOpponentClub(savedSchedule, command.clubId(), matchDetail);
        }

        return savedSchedule;
    }

    private void sendNotificationToOpponentClub(
            final Schedule schedule,
            final String myClubId,
            final MatchDetailData matchDetail
    ) {
        // 상대팀 클럽 ID 결정 (내 클럽이 홈이면 어웨이가 상대, 내 클럽이 어웨이면 홈이 상대)
        final String opponentClubId = myClubId.equals(matchDetail.homeClubId())
                ? matchDetail.awayClubId()
                : matchDetail.homeClubId();

        if (isNull(opponentClubId) || opponentClubId.isEmpty()) {
            return; // 상대팀 클럽 ID가 없으면 알림 전송 안함 (등록되지 않은 팀)
        }

        // 상대팀 클럽이 실제로 등록된 클럽인지 확인
        final Club opponentClub = clubRepository.findById(opponentClubId);
        if (isNull(opponentClub)) {
            return; // 등록되지 않은 클럽이면 알림 전송 안함
        }

        // 내 클럽 정보 조회
        final Club myClub = clubRepository.findById(myClubId);
        if (isNull(myClub)) {
            return;
        }

        // 알림 전송
        notificationService.createScheduleLinkNotification(
                opponentClubId,
                schedule.getId(),
                myClubId,
                myClub.getName(),
                schedule.getTitle(),
                schedule.getScheduleTime().toString()
        );
    }

    @Override
    public Schedule cancel(final String userId, final String clubId, final String scheduleId) {
        verifyStaff(userId, clubId);
        final Schedule schedule = schedule(scheduleId);
        schedule.cancel();
        return scheduleRepository.save(schedule);
    }

    @Override
    public Schedule respond(final RespondToScheduleCommand command) {
        final Club club = clubRepository.findById(command.clubId());
        if (isNull(club) || club.hasNotUser(command.userId())) {
            throw new IllegalArgumentException();
        }
        final Schedule schedule = schedule(command.scheduleId());
        schedule.respond(command.userId(), command.status(), command.reason());
        return scheduleRepository.save(schedule);
    }

    @Override
    public Schedule update(final UpdateScheduleCommand command) {
        verifyStaff(command.userId(), command.clubId());
        final Schedule schedule = schedule(command.scheduleId());
        schedule.update(
                command.title(), command.content(), command.location(), command.scheduleTime(), command.detail(),
                command.minParticipants(), command.maxParticipants(), command.deadlineDays()
        );
        return scheduleRepository.save(schedule);
    }

    private void verifyStaff(final String userId, final String clubId) {
        final Club club = clubRepository.findById(clubId);
        if (isNull(club)) {
            throw new IllegalArgumentException();
        }
        club.getMembers().stream()
                .filter(Member::isStaff)
                .filter(it -> it.isSame(userId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    private List<String> attendanceUserIds(final String clubId) {
        final Club club = clubRepository.findById(clubId);
        if (isNull(club)) {
            throw new IllegalArgumentException();
        }
        return club.getMembers()
                .stream()
                .map(Member::getUserId)
                .toList();
    }

    private Schedule schedule(final String scheduleId) {
        final Schedule schedule = scheduleRepository.findById(scheduleId);
        if (isNull(schedule)) {
            throw new IllegalArgumentException("Schedule not found: " + scheduleId);
        }
        return schedule;
    }
}
