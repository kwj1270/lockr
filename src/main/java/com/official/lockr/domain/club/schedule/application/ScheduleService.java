package com.official.lockr.domain.club.schedule.application;

import com.official.lockr.domain.club.schedule.application.command.AdminUpdateAttendanceCommand;
import com.official.lockr.domain.club.schedule.application.command.CancelScheduleCommand;
import com.official.lockr.domain.club.schedule.application.command.CreateScheduleCommand;
import com.official.lockr.domain.club.schedule.application.command.RespondToScheduleCommand;
import com.official.lockr.domain.club.schedule.application.command.UpdateScheduleCommand;
import com.official.lockr.domain.club.schedule.application.usecase.AdminUpdateAttendanceUseCase;
import com.official.lockr.domain.club.schedule.application.usecase.CancelScheduleUseCase;
import com.official.lockr.domain.club.schedule.application.usecase.RegisterScheduleUseCase;
import com.official.lockr.domain.club.schedule.application.usecase.RespondToScheduleUseCase;
import com.official.lockr.domain.club.schedule.application.usecase.UpdateScheduleUseCase;
import com.official.lockr.domain.club.schedule.domain.Schedule;
import com.official.lockr.domain.club.schedule.domain.ScheduleClub;
import com.official.lockr.domain.club.schedule.domain.ScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class ScheduleService implements RegisterScheduleUseCase, CancelScheduleUseCase, RespondToScheduleUseCase, UpdateScheduleUseCase, AdminUpdateAttendanceUseCase {

    private final ScheduleClub scheduleClub;
    private final ScheduleRepository scheduleRepository;

    public ScheduleService(
            final ScheduleClub scheduleClub,
            final ScheduleRepository scheduleRepository
    ) {
        this.scheduleClub = scheduleClub;
        this.scheduleRepository = scheduleRepository;
    }

    @Override
public Schedule create(final CreateScheduleCommand command) {
        verifyStaff(command.userId(), command.clubId());
        final Schedule schedule = Schedule.create(
                generateUlid(), command.clubId(), command.userId(), command.title(), command.content(), command.location(),
                command.scheduleTime(), command.scheduleType(), command.detail(), attendanceUserIds(command.clubId()),
                command.minParticipants(), command.deadlineDays(), LocalDateTime.now()
        );
        return scheduleRepository.save(schedule);
    }

    @Override
public Schedule cancel(final CancelScheduleCommand command) {
        verifyStaff(command.userId(), command.clubId());
        final Schedule schedule = schedule(command.scheduleId(), command.clubId());
        schedule.cancel();
        return scheduleRepository.save(schedule);
    }

    @Override
public Schedule respond(final RespondToScheduleCommand command) {
        verifyMember(command.userId(), command.clubId());
        final Schedule schedule = schedule(command.scheduleId(), command.clubId());
        schedule.respond(command.userId(), command.status(), command.reason());
        return scheduleRepository.save(schedule);
    }

    @Override
public Schedule update(final UpdateScheduleCommand command) {
        verifyStaff(command.userId(), command.clubId());
        final Schedule schedule = schedule(command.scheduleId(), command.clubId());
        schedule.update(
                command.title(), command.content(), command.location(), command.scheduleTime(), command.detail(),
                command.minParticipants(), command.deadlineDays()
        );
        return scheduleRepository.save(schedule);
    }

    @Override
public void update(final AdminUpdateAttendanceCommand command) {
        final String adminRole = verifyStaffAndGetRole(command.adminUserId(), command.clubId());
        final Schedule schedule = schedule(command.scheduleId(), command.clubId());
        schedule.adminRespond(command.targetUserId(), command.adminUserId(), adminRole, command.status(), command.reason());
        scheduleRepository.save(schedule);
    }

    private void verifyStaff(final String userId, final String clubId) {
        verifyStaffAndGetRole(userId, clubId);
    }

    private String verifyStaffAndGetRole(final String userId, final String clubId) {
        final String roleName = scheduleClub.findStaffRoleName(userId, clubId);
        if (isNull(roleName)) {
            throw new IllegalArgumentException("User is not staff member: " + userId);
        }
        return roleName;
    }

    private List<String> attendanceUserIds(final String clubId) {
        return scheduleClub.findAllUserIdsByClubId(clubId);
    }

    private void verifyMember(final String userId, final String clubId) {
        if (!scheduleClub.isMember(userId, clubId)) {
            throw new IllegalArgumentException("User is not club member: " + userId);
        }
    }

    private Schedule schedule(final String scheduleId, final String clubId) {
        final Schedule schedule = scheduleRepository.findById(scheduleId);
        if (isNull(schedule)) {
            throw new IllegalArgumentException("Schedule not found: " + scheduleId);
        }
        if (!schedule.getClubId().equals(clubId)) {
            throw new IllegalArgumentException("Schedule does not belong to club: " + clubId);
        }
        return schedule;
    }
}
