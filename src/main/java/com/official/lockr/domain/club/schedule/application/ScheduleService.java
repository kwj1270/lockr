package com.official.lockr.domain.club.schedule.application;

import com.official.lockr.domain.club.club.domain.Member;
import com.official.lockr.domain.club.schedule.application.dto.CreateScheduleCommand;
import com.official.lockr.domain.club.schedule.application.dto.RespondToScheduleCommand;
import com.official.lockr.domain.club.schedule.application.dto.UpdateScheduleCommand;
import com.official.lockr.domain.club.schedule.application.usecase.CancelScheduleUseCase;
import com.official.lockr.domain.club.schedule.application.usecase.CreateScheduleUseCase;
import com.official.lockr.domain.club.schedule.application.usecase.RespondToScheduleUseCase;
import com.official.lockr.domain.club.schedule.application.usecase.UpdateScheduleUseCase;
import com.official.lockr.domain.club.schedule.domain.Schedule;
import com.official.lockr.domain.club.schedule.domain.ScheduleClub;
import com.official.lockr.domain.club.schedule.domain.ScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class ScheduleService implements CreateScheduleUseCase, CancelScheduleUseCase, RespondToScheduleUseCase, UpdateScheduleUseCase {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleClub scheduleClub;

    public ScheduleService(
            final ScheduleRepository scheduleRepository,
            final ScheduleClub scheduleClub
    ) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleClub = scheduleClub;
    }

    @Override
    public Schedule create(final CreateScheduleCommand command) {
        final Member scheduleManager = scheduleManager(command.userId(), command.clubId());
        final Schedule schedule = Schedule.create(
                generateUlid(),
                command.clubId(),
                command.title(),
                command.content(),
                command.location(),
                command.scheduleTime(),
                command.scheduleType(),
                command.detail(),
                attendanceUserIds(scheduleManager.getClubId())
        );
        return scheduleRepository.save(schedule);
    }

    public Schedule schedule(final String scheduleId) {
        final Schedule schedule = scheduleRepository.findById(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule not found: " + scheduleId);
        }
        return schedule;
    }

    private List<String> attendanceUserIds(final String clubId) {
        final List<String> attendanceUserIds = scheduleClub.findAllMemberIdsByClubId(clubId)
                .stream()
                .map(Member::getUserId)
                .toList();
        if (attendanceUserIds.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return attendanceUserIds;
    }

    private Member scheduleManager(final String userId, final String clubId) {
        final Member scheduleCreator = scheduleClub.findMemberByUserIdAndClubId(userId, clubId);
        if (isNull(scheduleCreator) || !((scheduleCreator.isManager() || scheduleCreator.isPresident()))) {
            throw new IllegalArgumentException();
        }
        return scheduleCreator;
    }

    public List<Schedule> getSchedulesByClubId(final String clubId) {
        return scheduleRepository.findAllByClubId(clubId);
    }

    public List<Schedule> getSchedulesByClubIdAndMonth(final String clubId, final YearMonth yearMonth) {
        return scheduleRepository.findAllByClubIdAndMonth(clubId, yearMonth);
    }

    @Override
    public Schedule cancel(final String userId, final String clubId, final String scheduleId) {
        scheduleManager(userId, clubId);
        final Schedule schedule = scheduleRepository.findById(scheduleId);
        if (isNull(schedule)) {
            throw new IllegalArgumentException();
        }
        schedule.cancel();
        return scheduleRepository.save(schedule);
    }

    @Override
    public Schedule respond(final RespondToScheduleCommand command) {
        final List<Member> clubMembers = scheduleClub.findAllMemberIdsByClubId(command.clubId());
        if (clubMembers.stream().noneMatch(it -> it.isSame(command.userId()))) {
            throw new IllegalArgumentException();
        }
        final Schedule schedule = scheduleRepository.findById(command.scheduleId());
        if (isNull(schedule)) {
            throw new IllegalArgumentException();
        }
        schedule.respond(command.userId(), command.status());
        return scheduleRepository.save(schedule);
    }

    @Override
    public Schedule update(final UpdateScheduleCommand command) {
        scheduleManager(command.userId(), command.clubId());
        final Schedule schedule = scheduleRepository.findById(command.scheduleId());
        if (isNull(schedule)) {
            throw new IllegalArgumentException();
        }
        schedule.update(command.title(), command.content(), command.location(), command.scheduleTime(), command.detail());
        return scheduleRepository.save(schedule);
    }
}
