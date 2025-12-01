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
import org.springframework.stereotype.Service;

import java.util.List;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class ScheduleService implements RegisterScheduleUseCase, CancelScheduleUseCase, RespondToScheduleUseCase, UpdateScheduleUseCase {

    private final ClubRepository clubRepository;
    private final ScheduleRepository scheduleRepository;

    public ScheduleService(final ClubRepository clubRepository, final ScheduleRepository scheduleRepository
    ) {
        this.clubRepository = clubRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    public Schedule create(final CreateScheduleCommand command) {
        verifyStaff(command.userId(), command.clubId());
        final Schedule schedule = Schedule.create(
                generateUlid(), command.clubId(), command.title(), command.content(), command.location(),
                command.scheduleTime(), command.scheduleType(), command.detail(), attendanceUserIds(command.clubId()),
                command.minParticipants(), command.maxParticipants(), command.deadlineDays()
        );
        return scheduleRepository.save(schedule);
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
        if (isNull(club) || !club.hasNotUser(command.userId())) {
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
