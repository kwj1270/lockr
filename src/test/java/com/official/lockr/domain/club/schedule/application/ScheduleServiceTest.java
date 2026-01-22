package com.official.lockr.domain.club.schedule.application;

import com.official.lockr.domain.club.club.domain.Club;
import com.official.lockr.domain.club.club.domain.ClubRepository;
import com.official.lockr.domain.club.club.domain.Member;
import com.official.lockr.domain.club.schedule.application.dto.CreateScheduleCommand;
import com.official.lockr.domain.club.schedule.application.dto.RespondToScheduleCommand;
import com.official.lockr.domain.club.schedule.application.dto.UpdateScheduleCommand;
import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;
import com.official.lockr.domain.club.schedule.domain.Schedule;
import com.official.lockr.domain.club.schedule.domain.ScheduleRepository;
import com.official.lockr.domain.club.schedule.domain.ScheduleStatus;
import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.official.lockr.domain.club.schedule.domain.vo.TrainingDetailData;
import com.official.lockr.domain.notification.application.NotificationService;
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

    private ClubRepository clubRepository;
    private ScheduleRepository scheduleRepository;
    private NotificationService notificationService;
    private ScheduleService scheduleService;

    @BeforeEach
    void setUp() {
        clubRepository = mock(ClubRepository.class);
        scheduleRepository = mock(ScheduleRepository.class);
        notificationService = mock(NotificationService.class);
        scheduleService = new ScheduleService(clubRepository, scheduleRepository, notificationService);
    }

    @Test
    @DisplayName("일정 생성 시 멤버들에게 Attendance가 자동 생성되어야 한다")
    void shouldCreateAttendancesForAllMembersWhenCreatingSchedule() {
        // given
        String staffUserId = "user-001";
        String clubId = "club-001";

        Member president = Member.president(staffUserId, clubId, null);
        Member member2 = Member.basic("user-002", clubId, null);
        Member member3 = Member.basic("user-003", clubId, null);

        Club club = new Club(
                clubId, staffUserId, "테스트 클럽", "FOOTBALL",
                "서울", "강남구", "테스트 클럽입니다",
                null, null,
                List.of(president, member2, member3),
                LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(clubRepository.findById(clubId)).thenReturn(club);
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

        Member president = Member.president("user-001", clubId, null);
        Member member2 = Member.basic("user-002", clubId, null);

        Club club = new Club(
                clubId, "user-001", "테스트 클럽", "FOOTBALL",
                "서울", "강남구", "테스트 클럽입니다",
                null, null,
                List.of(president, member2),
                LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(clubRepository.findById(clubId)).thenReturn(club);

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

        Member president = Member.president("user-001", clubId, null);
        Member member2 = Member.basic(memberUserId, clubId, null);

        Club club = new Club(
                clubId, "user-001", "테스트 클럽", "FOOTBALL",
                "서울", "강남구", "테스트 클럽입니다",
                null, null,
                List.of(president, member2),
                LocalDateTime.now(), LocalDateTime.now(), null
        );

        Schedule schedule = Schedule.create(
                scheduleId, clubId, "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(),
                List.of("user-001", memberUserId),
                5, 20, 3
        );

        when(clubRepository.findById(clubId)).thenReturn(club);
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

        Member president = Member.president("user-001", clubId, null);
        Member basicMember = Member.basic(basicMemberUserId, clubId, null);

        Club club = new Club(
                clubId, "user-001", "테스트 클럽", "FOOTBALL",
                "서울", "강남구", "테스트 클럽입니다",
                null, null,
                List.of(president, basicMember),
                LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(clubRepository.findById(clubId)).thenReturn(club);

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

        Member president = Member.president(staffUserId, clubId, null);
        Member basicMember = Member.basic("user-002", clubId, null);

        Club club = new Club(
                clubId, staffUserId, "테스트 클럽", "FOOTBALL",
                "서울", "강남구", "테스트 클럽입니다",
                null, null,
                List.of(president, basicMember),
                LocalDateTime.now(), LocalDateTime.now(), null
        );

        Schedule schedule = Schedule.create(
                scheduleId, clubId, "원래 제목", "원래 내용", "원래 장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(),
                List.of(staffUserId, "user-002"),
                5, 20, 3
        );

        when(clubRepository.findById(clubId)).thenReturn(club);
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

        Member president = Member.president("user-001", clubId, null);
        Member basicMember = Member.basic(basicMemberUserId, clubId, null);

        Club club = new Club(
                clubId, "user-001", "테스트 클럽", "FOOTBALL",
                "서울", "강남구", "테스트 클럽입니다",
                null, null,
                List.of(president, basicMember),
                LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(clubRepository.findById(clubId)).thenReturn(club);

        // when & then
        assertThatThrownBy(() -> scheduleService.cancel(basicMemberUserId, clubId, scheduleId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("스태프만 일정을 취소할 수 있어야 한다 - 스태프인 경우 성공")
    void shouldAllowStaffToCancel() {
        // given
        String clubId = "club-001";
        String scheduleId = "schedule-001";
        String staffUserId = "user-001";

        Member president = Member.president(staffUserId, clubId, null);
        Member basicMember = Member.basic("user-002", clubId, null);

        Club club = new Club(
                clubId, staffUserId, "테스트 클럽", "FOOTBALL",
                "서울", "강남구", "테스트 클럽입니다",
                null, null,
                List.of(president, basicMember),
                LocalDateTime.now(), LocalDateTime.now(), null
        );

        Schedule schedule = Schedule.create(
                scheduleId, clubId, "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(),
                List.of(staffUserId, "user-002"),
                5, 20, 3
        );

        when(clubRepository.findById(clubId)).thenReturn(club);
        when(scheduleRepository.findById(scheduleId)).thenReturn(schedule);
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Schedule result = scheduleService.cancel(staffUserId, clubId, scheduleId);

        // then
        assertThat(result.getStatus()).isEqualTo(ScheduleStatus.CANCELLED);
        assertThat(result.isCancelled()).isTrue();
    }
}
