package com.official.lockr.domain.club.schedule.application;

import com.official.lockr.domain.club.schedule.application.command.AdminUpdateAttendanceCommand;
import com.official.lockr.domain.club.schedule.application.command.CancelScheduleCommand;
import com.official.lockr.domain.club.schedule.application.command.CreateScheduleCommand;
import com.official.lockr.domain.club.schedule.application.command.RespondToScheduleCommand;
import com.official.lockr.domain.club.schedule.application.command.UpdateScheduleCommand;
import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;
import com.official.lockr.domain.club.schedule.domain.Schedule;
import com.official.lockr.domain.club.schedule.domain.ScheduleClub;
import com.official.lockr.domain.club.schedule.domain.ScheduleRepository;
import com.official.lockr.domain.club.schedule.domain.ScheduleStatus;
import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.official.lockr.domain.club.schedule.domain.vo.TrainingDetailData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScheduleServiceTest {

    private ScheduleClub scheduleClub;
    private ScheduleRepository scheduleRepository;
    private ScheduleService scheduleService;

    @BeforeEach
    void setUp() {
        scheduleClub = mock(ScheduleClub.class);
        scheduleRepository = mock(ScheduleRepository.class);
        scheduleService = new ScheduleService(scheduleClub, scheduleRepository);
    }

    @Test
    @DisplayName("일정 생성 시 멤버들에게 Attendance가 자동 생성되어야 한다")
    void shouldCreateAttendancesForAllMembersWhenCreatingSchedule() {
        // given
        String staffUserId = "user-001";
        String clubId = "club-001";

        when(scheduleClub.findStaffRoleName(staffUserId, clubId)).thenReturn("PRESIDENT");
        when(scheduleClub.findAllUserIdsByClubId(clubId)).thenReturn(List.of("user-001", "user-002", "user-003"));
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateScheduleCommand command = new CreateScheduleCommand(
                staffUserId,
                clubId,
                "주간 훈련",
                "정기 훈련입니다",
                "축구장 A",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING,
                new TrainingDetailData(),
                5,
                20,
                3
        );

        // when
        Schedule schedule = scheduleService.create(command);

        // then
        assertThat(schedule.getAttendances()).hasSize(3);
        assertThat(schedule.getAttendances())
                .extracting("userId")
                .containsExactlyInAnyOrder("user-001", "user-002", "user-003");
        assertThat(schedule.getAttendances())
                .allMatch(attendance -> attendance.getStatus() == AttendanceStatus.NO_RESPONSE);
    }

    @Test
    @DisplayName("클럽 멤버만 응답할 수 있어야 한다 - 멤버가 아닌 경우 예외")
    void shouldThrowExceptionWhenNonMemberTriesToRespond() {
        // given
        String clubId = "club-001";
        String scheduleId = "schedule-001";
        String nonMemberUserId = "non-member-user";

        when(scheduleClub.isMember(nonMemberUserId, clubId)).thenReturn(false);

        RespondToScheduleCommand command = new RespondToScheduleCommand(
                scheduleId,
                nonMemberUserId,
                clubId,
                AttendanceStatus.ATTENDING,
                "참석합니다"
        );

        // when & then
        assertThatThrownBy(() -> scheduleService.respond(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("클럽 멤버만 응답할 수 있어야 한다 - 멤버인 경우 성공")
    void shouldAllowMemberToRespond() {
        // given
        String clubId = "club-001";
        String scheduleId = "schedule-001";
        String memberUserId = "user-002";

        Schedule schedule = Schedule.create(
                scheduleId, clubId, "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(),
                List.of("user-001", memberUserId),
                5, 20, 3, LocalDateTime.now()
        );

        when(scheduleClub.isMember(memberUserId, clubId)).thenReturn(true);
        when(scheduleRepository.findById(scheduleId)).thenReturn(schedule);
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RespondToScheduleCommand command = new RespondToScheduleCommand(
                scheduleId,
                memberUserId,
                clubId,
                AttendanceStatus.ATTENDING,
                "참석합니다"
        );

        // when
        Schedule result = scheduleService.respond(command);

        // then
        assertThat(result.getAttendances())
                .filteredOn(a -> a.getUserId().equals(memberUserId))
                .allMatch(a -> a.getStatus() == AttendanceStatus.ATTENDING);
    }

    @Test
    @DisplayName("스태프만 일정을 수정할 수 있어야 한다 - 일반 멤버인 경우 예외")
    void shouldThrowExceptionWhenNonStaffTriesToUpdate() {
        // given
        String clubId = "club-001";
        String scheduleId = "schedule-001";
        String basicMemberUserId = "user-002";

        when(scheduleClub.findStaffRoleName(basicMemberUserId, clubId)).thenReturn(null);

        UpdateScheduleCommand command = new UpdateScheduleCommand(
                scheduleId,
                basicMemberUserId,
                clubId,
                "변경된 제목",
                "변경된 내용",
                "변경된 장소",
                LocalDateTime.now().plusDays(14),
                new TrainingDetailData(),
                10, 25, 5
        );

        // when & then
        assertThatThrownBy(() -> scheduleService.update(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("스태프만 일정을 수정할 수 있어야 한다 - 스태프인 경우 성공")
    void shouldAllowStaffToUpdate() {
        // given
        String clubId = "club-001";
        String scheduleId = "schedule-001";
        String staffUserId = "user-001";

        Schedule schedule = Schedule.create(
                scheduleId, clubId, "원래 제목", "원래 내용", "원래 장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(),
                List.of(staffUserId, "user-002"),
                5, 20, 3, LocalDateTime.now()
        );

        when(scheduleClub.findStaffRoleName(staffUserId, clubId)).thenReturn("PRESIDENT");
        when(scheduleRepository.findById(scheduleId)).thenReturn(schedule);
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String newTitle = "변경된 제목";
        LocalDateTime newTime = LocalDateTime.now().plusDays(14);

        UpdateScheduleCommand command = new UpdateScheduleCommand(
                scheduleId,
                staffUserId,
                clubId,
                newTitle,
                "변경된 내용",
                "변경된 장소",
                newTime,
                new TrainingDetailData(),
                10, 25, 5
        );

        // when
        Schedule result = scheduleService.update(command);

        // then
        assertThat(result.getTitle()).isEqualTo(newTitle);
        assertThat(result.getScheduleTime()).isEqualTo(newTime);
    }

    @Test
    @DisplayName("스태프만 일정을 취소할 수 있어야 한다 - 일반 멤버인 경우 예외")
    void shouldThrowExceptionWhenNonStaffTriesToCancel() {
        // given
        String clubId = "club-001";
        String scheduleId = "schedule-001";
        String basicMemberUserId = "user-002";

        when(scheduleClub.findStaffRoleName(basicMemberUserId, clubId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> scheduleService.cancel(new CancelScheduleCommand(basicMemberUserId, clubId, scheduleId)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("스태프만 일정을 취소할 수 있어야 한다 - 스태프인 경우 성공")
    void shouldAllowStaffToCancel() {
        // given
        String clubId = "club-001";
        String scheduleId = "schedule-001";
        String staffUserId = "user-001";

        Schedule schedule = Schedule.create(
                scheduleId, clubId, "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(),
                List.of(staffUserId, "user-002"),
                5, 20, 3, LocalDateTime.now()
        );

        when(scheduleClub.findStaffRoleName(staffUserId, clubId)).thenReturn("PRESIDENT");
        when(scheduleRepository.findById(scheduleId)).thenReturn(schedule);
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Schedule result = scheduleService.cancel(new CancelScheduleCommand(staffUserId, clubId, scheduleId));

        // then
        assertThat(result.getStatus()).isEqualTo(ScheduleStatus.CANCELLED);
        assertThat(result.isCancelled()).isTrue();
    }

    // === C1: IDOR 보안 - Schedule이 해당 Club에 속하는지 검증 ===

    @Test
    @DisplayName("다른 클럽의 스케줄을 취소하려 하면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenCancellingScheduleOfDifferentClub() {
        // given
        String staffUserId = "user-001";
        String attackerClubId = "club-attacker";
        String victimClubId = "club-victim";
        String scheduleId = "schedule-001";

        Schedule schedule = Schedule.create(
                scheduleId, victimClubId, "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(),
                List.of("user-002"),
                5, 20, 3, LocalDateTime.now()
        );

        when(scheduleClub.findStaffRoleName(staffUserId, attackerClubId)).thenReturn("PRESIDENT");
        when(scheduleRepository.findById(scheduleId)).thenReturn(schedule);

        // when & then
        assertThatThrownBy(() -> scheduleService.cancel(new CancelScheduleCommand(staffUserId, attackerClubId, scheduleId)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to club");
    }

    @Test
    @DisplayName("다른 클럽의 스케줄을 수정하려 하면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenUpdatingScheduleOfDifferentClub() {
        // given
        String staffUserId = "user-001";
        String attackerClubId = "club-attacker";
        String victimClubId = "club-victim";
        String scheduleId = "schedule-001";

        Schedule schedule = Schedule.create(
                scheduleId, victimClubId, "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(),
                List.of("user-002"),
                5, 20, 3, LocalDateTime.now()
        );

        when(scheduleClub.findStaffRoleName(staffUserId, attackerClubId)).thenReturn("PRESIDENT");
        when(scheduleRepository.findById(scheduleId)).thenReturn(schedule);

        UpdateScheduleCommand command = new UpdateScheduleCommand(
                scheduleId, staffUserId, attackerClubId,
                "변경", "변경", "변경",
                LocalDateTime.now().plusDays(14),
                new TrainingDetailData(), 10, 25, 5
        );

        // when & then
        assertThatThrownBy(() -> scheduleService.update(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to club");
    }

    // === C7: AdminUpdateAttendanceUseCase 테스트 ===

    @Test
    @DisplayName("스태프가 출석 상태를 변경할 수 있어야 한다")
    void shouldAllowStaffToAdminUpdateAttendance() {
        // given
        String clubId = "club-001";
        String scheduleId = "schedule-001";
        String adminUserId = "user-001";
        String targetUserId = "user-002";

        Schedule schedule = Schedule.create(
                scheduleId, clubId, "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(),
                List.of(adminUserId, targetUserId),
                5, 20, 3, LocalDateTime.now()
        );

        when(scheduleClub.findStaffRoleName(adminUserId, clubId)).thenReturn("MANAGER");
        when(scheduleRepository.findById(scheduleId)).thenReturn(schedule);
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminUpdateAttendanceCommand command = new AdminUpdateAttendanceCommand(
                scheduleId, adminUserId, clubId, targetUserId,
                AttendanceStatus.ATTENDING, "출석 처리"
        );

        // when
        scheduleService.update(command);

        // then
        assertThat(schedule.getAttendances())
                .filteredOn(a -> a.getUserId().equals(targetUserId))
                .allMatch(a -> a.getStatus() == AttendanceStatus.ATTENDING);
    }

    @Test
    @DisplayName("비스태프가 출석 상태를 변경하려 하면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenNonStaffTriesToAdminUpdateAttendance() {
        // given
        String clubId = "club-001";
        String scheduleId = "schedule-001";
        String nonStaffUserId = "user-002";

        when(scheduleClub.findStaffRoleName(nonStaffUserId, clubId)).thenReturn(null);

        AdminUpdateAttendanceCommand command = new AdminUpdateAttendanceCommand(
                scheduleId, nonStaffUserId, clubId, "user-003",
                AttendanceStatus.ATTENDING, "출석 처리"
        );

        // when & then
        assertThatThrownBy(() -> scheduleService.update(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not staff");
    }

    // === C8: findById null 반환 시 예외 처리 테스트 ===

    @Test
    @DisplayName("존재하지 않는 일정에 응답하면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenRespondToNonExistentSchedule() {
        // given
        String clubId = "club-001";
        String nonExistentScheduleId = "non-existent";
        String userId = "user-001";

        when(scheduleClub.isMember(userId, clubId)).thenReturn(true);
        when(scheduleRepository.findById(nonExistentScheduleId)).thenReturn(null);

        RespondToScheduleCommand command = new RespondToScheduleCommand(
                nonExistentScheduleId, userId, clubId,
                AttendanceStatus.ATTENDING, "참석"
        );

        // when & then
        assertThatThrownBy(() -> scheduleService.respond(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Schedule not found");
    }

    @Test
    @DisplayName("존재하지 않는 일정을 수정하면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenUpdateNonExistentSchedule() {
        // given
        String clubId = "club-001";
        String nonExistentScheduleId = "non-existent";
        String staffUserId = "user-001";

        when(scheduleClub.findStaffRoleName(staffUserId, clubId)).thenReturn("PRESIDENT");
        when(scheduleRepository.findById(nonExistentScheduleId)).thenReturn(null);

        UpdateScheduleCommand command = new UpdateScheduleCommand(
                nonExistentScheduleId, staffUserId, clubId,
                "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                new TrainingDetailData(), 5, 20, 3
        );

        // when & then
        assertThatThrownBy(() -> scheduleService.update(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Schedule not found");
    }

    @Test
    @DisplayName("존재하지 않는 일정을 취소하면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenCancelNonExistentSchedule() {
        // given
        String clubId = "club-001";
        String nonExistentScheduleId = "non-existent";
        String staffUserId = "user-001";

        when(scheduleClub.findStaffRoleName(staffUserId, clubId)).thenReturn("PRESIDENT");
        when(scheduleRepository.findById(nonExistentScheduleId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> scheduleService.cancel(new CancelScheduleCommand(staffUserId, clubId, nonExistentScheduleId)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Schedule not found");
    }
}
