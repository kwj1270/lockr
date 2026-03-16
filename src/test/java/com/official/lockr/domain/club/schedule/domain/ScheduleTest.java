package com.official.lockr.domain.club.schedule.domain;

import com.official.lockr.domain.club.schedule.domain.event.AttendanceStatusChangedEvent;
import com.official.lockr.domain.club.schedule.domain.event.CancelledScheduleEvent;
import com.official.lockr.domain.club.schedule.domain.event.CreatedScheduleEvent;
import com.official.lockr.domain.club.schedule.domain.event.UpdatedScheduleEvent;
import com.official.lockr.domain.club.schedule.domain.vo.MatchDetailData;
import com.official.lockr.domain.club.schedule.domain.vo.SocialDetailData;
import com.official.lockr.domain.club.schedule.domain.vo.TrainingDetailData;
import com.official.lockr.global.ddd.DomainEvent;
import com.official.lockr.global.ddd.DomainEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScheduleTest {

    @DisplayName("일정 생성 시 필수 필드가 설정되어야 한다")
    @Test
    void shouldSetRequiredFieldsWhenCreatingSchedule() {
        // given
        final String id = "schedule-001";
        final String clubId = "club-001";
        final String title = "주간 훈련";
        final String content = "정기 훈련입니다";
        final String location = "축구장 A";
        final LocalDateTime scheduleTime = LocalDateTime.now().plusDays(7);
        final ScheduleType scheduleType = ScheduleType.TRAINING;
        final TrainingDetailData detail = new TrainingDetailData();
        final List<String> userIds = List.of("user-001", "user-002");
        final int minParticipants = 5;
        final int deadlineDays = 3;

        // when
        final Schedule schedule = Schedule.create(
                id, clubId, "user-001", title, content, location, scheduleTime,
                scheduleType, detail, userIds, minParticipants, deadlineDays,
                LocalDateTime.now()
        );

        // then
        assertThat(schedule.getId()).isEqualTo(id);
        assertThat(schedule.getClubId()).isEqualTo(clubId);
        assertThat(schedule.getTitle()).isEqualTo(title);
        assertThat(schedule.getContent()).isEqualTo(content);
        assertThat(schedule.getLocation()).isEqualTo(location);
        assertThat(schedule.getScheduleTime()).isEqualTo(scheduleTime);
        assertThat(schedule.getScheduleType()).isEqualTo(scheduleType);
        assertThat(schedule.getDetail()).isEqualTo(detail);
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.SCHEDULED);
        assertThat(schedule.getMinParticipants()).isEqualTo(minParticipants);
        assertThat(schedule.getDeadlineDays()).isEqualTo(deadlineDays);
        assertThat(schedule.getAttendances()).hasSize(2);
        assertThat(schedule.getCreatedAt()).isNotNull();
        assertThat(schedule.getUpdatedAt()).isNotNull();
    }

    @DisplayName("과거 시간으로 일정 생성 시 예외가 발생해야 한다")
    @Test
    void shouldThrowExceptionWhenScheduleTimeIsInThePast() {
        // given
        final LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        // when & then
        assertThatThrownBy(() -> Schedule.create(
                "schedule-001", "club-001", "user-001", "과거 일정", "내용", "장소", pastTime,
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, 3, LocalDateTime.now()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
    }

    @DisplayName("일정 생성 시 CreatedScheduleEvent가 발행되어야 한다")
    @Test
    void shouldPublishCreatedScheduleEventWhenCreatingSchedule() {
        // given
        final String id = "schedule-001";
        final String clubId = "club-001";
        final LocalDateTime scheduleTime = LocalDateTime.now().plusDays(7);
        final ScheduleType scheduleType = ScheduleType.TRAINING;

        final Schedule schedule = Schedule.create(
                id, clubId, "user-001", "훈련", "내용", "장소", scheduleTime,
                scheduleType, new TrainingDetailData(), List.of("user-001"),
                5, 3, LocalDateTime.now()
        );

        final List<DomainEvent> capturedEvents = new ArrayList<>();
        final DomainEventPublisher mockPublisher = capturedEvents::add;

        // when
        schedule.publish(mockPublisher);

        // then
        assertThat(capturedEvents).hasSize(1);
        assertThat(capturedEvents.get(0)).isInstanceOf(CreatedScheduleEvent.class);

        final CreatedScheduleEvent event = (CreatedScheduleEvent) capturedEvents.get(0);
        assertThat(event.scheduleId()).isEqualTo(id);
        assertThat(event.clubId()).isEqualTo(clubId);
        assertThat(event.scheduleType()).isEqualTo(scheduleType);
        assertThat(event.scheduleTime()).isEqualTo(scheduleTime);
    }

    @DisplayName("매치 타입 일정 생성 시 MatchDetailData가 설정되어야 한다")
    @Test
    void shouldSetMatchDetailDataWhenCreatingMatchTypeSchedule() {
        // given
        final String homeClubId = "club-001";
        final String awayClubId = "club-002";
        final String opponentName = "상대팀FC";
        final MatchDetailData matchDetail = new MatchDetailData(homeClubId, awayClubId, opponentName);

        // when
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "정기전", "내용", "경기장",
                LocalDateTime.now().plusDays(7),
                ScheduleType.MATCH, matchDetail, List.of("user-001"),
                11, 3, LocalDateTime.now()
        );

        // then
        assertThat(schedule.getScheduleType()).isEqualTo(ScheduleType.MATCH);
        assertThat(schedule.getDetail()).isInstanceOf(MatchDetailData.class);

        final MatchDetailData detail = (MatchDetailData) schedule.getDetail();
        assertThat(detail.homeClubId()).isEqualTo(homeClubId);
        assertThat(detail.awayClubId()).isEqualTo(awayClubId);
        assertThat(detail.opponentName()).isEqualTo(opponentName);
    }

    @DisplayName("훈련 타입 일정 생성 시 TrainingDetailData가 설정되어야 한다")
    @Test
    void shouldSetTrainingDetailDataWhenCreatingTrainingTypeSchedule() {
        // given
        final TrainingDetailData trainingDetail = new TrainingDetailData();

        // when
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "정기 훈련", "체력 훈련", "훈련장",
                LocalDateTime.now().plusDays(3),
                ScheduleType.TRAINING, trainingDetail, List.of("user-001"),
                5, 2, LocalDateTime.now()
        );

        // then
        assertThat(schedule.getScheduleType()).isEqualTo(ScheduleType.TRAINING);
        assertThat(schedule.getDetail()).isInstanceOf(TrainingDetailData.class);
    }

    @DisplayName("소셜 이벤트 타입 일정 생성 시 SocialDetailData가 설정되어야 한다")
    @Test
    void shouldSetSocialDetailDataWhenCreatingSocialEventTypeSchedule() {
        // given
        final SocialDetailData socialDetail = new SocialDetailData();

        // when
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "회식", "시즌 종료 회식", "레스토랑",
                LocalDateTime.now().plusDays(14),
                ScheduleType.SOCIAL_EVENT, socialDetail, List.of("user-001", "user-002"),
                10, 5, LocalDateTime.now()
        );

        // then
        assertThat(schedule.getScheduleType()).isEqualTo(ScheduleType.SOCIAL_EVENT);
        assertThat(schedule.getDetail()).isInstanceOf(SocialDetailData.class);
    }

    @DisplayName("일정 응답 시 출석 상태가 변경되어야 한다")
    @Test
    void shouldUpdateAttendanceStatusWhenRespondingToSchedule() {
        // given
        final String userId = "user-001";
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of(userId),
                5, 3, LocalDateTime.now()
        );

        // 초기 상태 확인
        assertThat(schedule.getAttendances().get(0).getStatus()).isEqualTo(AttendanceStatus.NO_RESPONSE);

        // when
        schedule.respond(userId, AttendanceStatus.ATTENDING, "참석합니다");

        // then
        final Attendance attendance = schedule.getAttendances().get(0);
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.ATTENDING);
        assertThat(attendance.getReason()).isEqualTo("참석합니다");
    }

    @DisplayName("사용자가 재응답 시 출석 상태가 업데이트되어야 한다")
    @Test
    void shouldUpdateStatusWhenUserRespondsAgain() {
        // given
        final String userId = "user-001";
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of(userId),
                5, 3, LocalDateTime.now()
        );

        // 첫 번째 응답
        schedule.respond(userId, AttendanceStatus.ATTENDING, "참석합니다");
        assertThat(schedule.getAttendances().get(0).getStatus()).isEqualTo(AttendanceStatus.ATTENDING);

        // when - 두 번째 응답 (불참으로 변경)
        schedule.respond(userId, AttendanceStatus.NOT_ATTENDING, "일정이 생겼습니다");

        // then
        final Attendance attendance = schedule.getAttendances().get(0);
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.NOT_ATTENDING);
        assertThat(attendance.getReason()).isEqualTo("일정이 생겼습니다");
    }

    @DisplayName("출석 응답 시 AttendanceStatusChangedEvent가 발행되어야 한다")
    @Test
    void shouldPublishAttendanceStatusChangedEventWhenResponding() {
        // given
        final String scheduleId = "schedule-001";
        final String clubId = "club-001";
        final String userId = "user-001";
        final Schedule schedule = Schedule.create(
                scheduleId, clubId, "user-001", "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of(userId),
                5, 3, LocalDateTime.now()
        );

        // CreatedScheduleEvent 소비
        final List<DomainEvent> capturedEvents = new ArrayList<>();
        schedule.publish(capturedEvents::add);
        capturedEvents.clear();

        // when
        schedule.respond(userId, AttendanceStatus.ATTENDING, "참석");
        schedule.publish(capturedEvents::add);

        // then
        assertThat(capturedEvents).hasSize(1);
        assertThat(capturedEvents.get(0)).isInstanceOf(AttendanceStatusChangedEvent.class);

        final AttendanceStatusChangedEvent changeEvent = (AttendanceStatusChangedEvent) capturedEvents.get(0);
        assertThat(changeEvent.scheduleId()).isEqualTo(scheduleId);
        assertThat(changeEvent.userId()).isEqualTo(userId);
        assertThat(changeEvent.changedBy()).isEqualTo(userId);
        assertThat(changeEvent.changedByRole()).isEqualTo("PLAYER");
        assertThat(changeEvent.previousStatus()).isEqualTo(AttendanceStatus.NO_RESPONSE);
        assertThat(changeEvent.newStatus()).isEqualTo(AttendanceStatus.ATTENDING);
    }

    @DisplayName("취소된 일정에 응답 시 예외가 발생해야 한다")
    @Test
    void shouldThrowExceptionWhenRespondingToCancelledSchedule() {
        // given
        final String userId = "user-001";
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of(userId),
                5, 3, LocalDateTime.now()
        );

        schedule.cancel();

        // when & then
        assertThatThrownBy(() -> schedule.respond(userId, AttendanceStatus.ATTENDING, "참석"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cancelled");
    }

    @DisplayName("일정 수정 시 필드가 업데이트되어야 한다")
    @Test
    void shouldUpdateFieldsWhenUpdatingSchedule() {
        // given
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "원래 제목", "원래 내용", "원래 장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, 3, LocalDateTime.now()
        );

        final String newTitle = "변경된 제목";
        final String newContent = "변경된 내용";
        final String newLocation = "변경된 장소";
        final LocalDateTime newTime = LocalDateTime.now().plusDays(14);
        final TrainingDetailData newDetail = new TrainingDetailData();

        // when
        schedule.update(newTitle, newContent, newLocation, newTime, newDetail, 10, 5);

        // then
        assertThat(schedule.getTitle()).isEqualTo(newTitle);
        assertThat(schedule.getContent()).isEqualTo(newContent);
        assertThat(schedule.getLocation()).isEqualTo(newLocation);
        assertThat(schedule.getScheduleTime()).isEqualTo(newTime);
        assertThat(schedule.getMinParticipants()).isEqualTo(10);
        assertThat(schedule.getDeadlineDays()).isEqualTo(5);
    }

    @DisplayName("일정 수정 시 UpdatedScheduleEvent가 발행되어야 한다")
    @Test
    void shouldPublishUpdatedScheduleEventWhenUpdating() {
        // given
        final String scheduleId = "schedule-001";
        final String clubId = "club-001";
        final Schedule schedule = Schedule.create(
                scheduleId, clubId, "user-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, 3, LocalDateTime.now()
        );

        final List<DomainEvent> capturedEvents = new ArrayList<>();
        schedule.publish(capturedEvents::add);
        capturedEvents.clear();

        final LocalDateTime newTime = LocalDateTime.now().plusDays(14);

        // when
        schedule.update("새 제목", "새 내용", "새 장소", newTime, new TrainingDetailData(), 10, 5);
        schedule.publish(capturedEvents::add);

        // then
        assertThat(capturedEvents).hasSize(1);
        assertThat(capturedEvents.get(0)).isInstanceOf(UpdatedScheduleEvent.class);

        final UpdatedScheduleEvent event = (UpdatedScheduleEvent) capturedEvents.get(0);
        assertThat(event.scheduleId()).isEqualTo(scheduleId);
        assertThat(event.clubId()).isEqualTo(clubId);
        assertThat(event.newScheduleTime()).isEqualTo(newTime);
    }

    @DisplayName("취소된 일정 수정 시 예외가 발생해야 한다")
    @Test
    void shouldThrowExceptionWhenUpdatingCancelledSchedule() {
        // given
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, 3, LocalDateTime.now()
        );

        schedule.cancel();

        // when & then
        assertThatThrownBy(() -> schedule.update(
                "새 제목", "새 내용", "새 장소",
                LocalDateTime.now().plusDays(14),
                new TrainingDetailData(), 10, 5
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cancelled");
    }

    @DisplayName("일정 취소 시 상태가 CANCELLED로 변경되어야 한다")
    @Test
    void shouldChangeStatusToCancelledWhenCancelling() {
        // given
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, 3, LocalDateTime.now()
        );

        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.SCHEDULED);

        // when
        schedule.cancel();

        // then
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.CANCELLED);
        assertThat(schedule.isCancelled()).isTrue();
        assertThat(schedule.getDeletedAt()).isNotNull();
    }

    @DisplayName("일정 취소 시 CancelledScheduleEvent가 발행되어야 한다")
    @Test
    void shouldPublishCancelledScheduleEventWhenCancelling() {
        // given
        final String scheduleId = "schedule-001";
        final String clubId = "club-001";
        final ScheduleType scheduleType = ScheduleType.TRAINING;
        final Schedule schedule = Schedule.create(
                scheduleId, clubId, "user-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                scheduleType, new TrainingDetailData(), List.of("user-001"),
                5, 3, LocalDateTime.now()
        );

        final List<DomainEvent> capturedEvents = new ArrayList<>();
        schedule.publish(capturedEvents::add);
        capturedEvents.clear();

        // when
        schedule.cancel();
        schedule.publish(capturedEvents::add);

        // then
        assertThat(capturedEvents).hasSize(1);
        assertThat(capturedEvents.get(0)).isInstanceOf(CancelledScheduleEvent.class);

        final CancelledScheduleEvent event = (CancelledScheduleEvent) capturedEvents.get(0);
        assertThat(event.scheduleId()).isEqualTo(scheduleId);
        assertThat(event.clubId()).isEqualTo(clubId);
        assertThat(event.scheduleType()).isEqualTo(scheduleType);
    }

    @DisplayName("이미 취소된 일정 재취소 시 예외가 발생해야 한다")
    @Test
    void shouldThrowExceptionWhenCancellingAlreadyCancelledSchedule() {
        // given
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, 3, LocalDateTime.now()
        );

        schedule.cancel();

        // when & then
        assertThatThrownBy(schedule::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cancelled");
    }

    @DisplayName("멤버 추가 시 NO_RESPONSE 상태의 출석이 생성되어야 한다")
    @Test
    void shouldCreateNoResponseAttendanceWhenAddingMember() {
        // given
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, 3, LocalDateTime.now()
        );

        assertThat(schedule.getAttendances()).hasSize(1);

        // when
        schedule.addMember("user-002");

        // then
        assertThat(schedule.getAttendances()).hasSize(2);

        final Attendance newAttendance = schedule.getAttendances().stream()
                .filter(a -> a.getUserId().equals("user-002"))
                .findFirst()
                .orElseThrow();

        assertThat(newAttendance.getStatus()).isEqualTo(AttendanceStatus.NO_RESPONSE);
    }

    @DisplayName("참석 멤버 수가 정확히 집계되어야 한다")
    @Test
    void shouldCountAttendingMembersCorrectly() {
        // given
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(),
                List.of("user-001", "user-002", "user-003"),
                5, 3, LocalDateTime.now()
        );

        schedule.respond("user-001", AttendanceStatus.ATTENDING, null);
        schedule.respond("user-002", AttendanceStatus.ATTENDING, null);
        schedule.respond("user-003", AttendanceStatus.NOT_ATTENDING, "불참");

        // when & then
        assertThat(schedule.getAttendingCount()).isEqualTo(2);
    }

    @DisplayName("불참 멤버 수가 정확히 집계되어야 한다")
    @Test
    void shouldCountNotAttendingMembersCorrectly() {
        // given
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(),
                List.of("user-001", "user-002", "user-003", "user-004"),
                5, 3, LocalDateTime.now()
        );

        schedule.respond("user-001", AttendanceStatus.ATTENDING, null);
        schedule.respond("user-002", AttendanceStatus.NOT_ATTENDING, "출장");
        schedule.respond("user-003", AttendanceStatus.NOT_ATTENDING, "부상");

        // when & then
        assertThat(schedule.getNotAttendingCount()).isEqualTo(2);
    }

    @DisplayName("미응답 멤버 수가 정확히 집계되어야 한다")
    @Test
    void shouldCountNoResponseMembersCorrectly() {
        // given
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(),
                List.of("user-001", "user-002", "user-003", "user-004", "user-005"),
                5, 3, LocalDateTime.now()
        );

        schedule.respond("user-001", AttendanceStatus.ATTENDING, null);
        schedule.respond("user-002", AttendanceStatus.NOT_ATTENDING, "출장");
        // user-003, user-004, user-005는 미응답

        // when & then
        assertThat(schedule.getNoResponseCount()).isEqualTo(3);
    }


    @DisplayName("마감일이 음수이면 예외가 발생해야 한다")
    @Test
    void shouldThrowExceptionWhenDeadlineDaysIsNegative() {
        // when & then
        assertThatThrownBy(() -> Schedule.create(
                "schedule-001", "club-001", "user-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, -1, LocalDateTime.now()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deadlineDays");
    }

    @DisplayName("최소 참가자가 0이면 제한 없음으로 허용되어야 한다")
    @Test
    void shouldAllowZeroMinParticipants() {
        // when (0 means no restriction)
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                0, 3, LocalDateTime.now()
        );

        // then
        assertThat(schedule.getMinParticipants()).isEqualTo(0);
    }

    @DisplayName("최소 참가자가 음수이면 예외가 발생해야 한다")
    @Test
    void shouldThrowExceptionWhenMinParticipantsIsNegative() {
        // when & then
        assertThatThrownBy(() -> Schedule.create(
                "schedule-001", "club-001", "user-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                -1, 3, LocalDateTime.now()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minParticipants");
    }

    // === Task 5.1: AdminUpdateAttendance 도메인 테스트 ===

    @DisplayName("관리자 출석 응답 시 올바른 이벤트가 발행되어야 한다")
    @Test
    void shouldPublishCorrectEventsWhenAdminResponds() {
        // given
        final String scheduleId = "schedule-001";
        final String clubId = "club-001";
        final String targetUserId = "user-001";
        final String adminUserId = "admin-001";
        final String adminRole = "MANAGER";

        final Schedule schedule = Schedule.create(
                scheduleId, clubId, "user-001", "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of(targetUserId),
                5, 3, LocalDateTime.now()
        );

        // CreatedScheduleEvent 소비
        final List<DomainEvent> capturedEvents = new ArrayList<>();
        schedule.publish(capturedEvents::add);
        capturedEvents.clear();

        // when
        schedule.adminRespond(targetUserId, adminUserId, adminRole, AttendanceStatus.ATTENDING, "출석 처리");
        schedule.publish(capturedEvents::add);

        // then
        assertThat(capturedEvents).hasSize(1);
        assertThat(capturedEvents.get(0)).isInstanceOf(AttendanceStatusChangedEvent.class);

        final AttendanceStatusChangedEvent changeEvent = (AttendanceStatusChangedEvent) capturedEvents.get(0);
        assertThat(changeEvent.scheduleId()).isEqualTo(scheduleId);
        assertThat(changeEvent.userId()).isEqualTo(targetUserId);
        assertThat(changeEvent.changedBy()).isEqualTo(adminUserId);
        assertThat(changeEvent.changedByRole()).isEqualTo(adminRole);
        assertThat(changeEvent.previousStatus()).isEqualTo(AttendanceStatus.NO_RESPONSE);
        assertThat(changeEvent.newStatus()).isEqualTo(AttendanceStatus.ATTENDING);
        assertThat(changeEvent.reason()).isEqualTo("출석 처리");
    }

    @DisplayName("초대되지 않은 사용자에 대해 관리자 응답 시 예외가 발생해야 한다")
    @Test
    void shouldThrowExceptionWhenAdminRespondsForUninvitedUser() {
        // given
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, 3, LocalDateTime.now()
        );

        // when & then
        assertThatThrownBy(() -> schedule.adminRespond(
                "non-invited-user", "admin-001", "MANAGER", AttendanceStatus.ATTENDING, "출석 처리"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not invited");
    }

    @DisplayName("취소된 일정에 관리자 응답 시 예외가 발생해야 한다")
    @Test
    void shouldThrowExceptionWhenAdminRespondsOnCancelledSchedule() {
        // given
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, 3, LocalDateTime.now()
        );
        schedule.cancel();

        // when & then
        assertThatThrownBy(() -> schedule.adminRespond(
                "user-001", "admin-001", "MANAGER", AttendanceStatus.ATTENDING, "출석 처리"
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cancelled");
    }

    // === C1: LocalDateTime.now() 시간 일관성 테스트 ===

    @DisplayName("일정 취소 시 updatedAt과 deletedAt이 동일해야 한다")
    @Test
    void shouldHaveSameUpdatedAtAndDeletedAtWhenCancelling() {
        // given
        final LocalDateTime now = LocalDateTime.of(2025, 6, 1, 10, 0);
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "제목", "내용", "장소",
                LocalDateTime.of(2025, 7, 1, 10, 0),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, 3, LocalDateTime.of(2025, 5, 1, 10, 0)
        );

        // when
        schedule.cancel(now);

        // then
        assertThat(schedule.getUpdatedAt()).isEqualTo(now);
        assertThat(schedule.getDeletedAt()).isEqualTo(now);
        assertThat(schedule.getUpdatedAt()).isEqualTo(schedule.getDeletedAt());
    }

    @DisplayName("일정 응답 시 updatedAt과 이벤트 changedAt이 동일해야 한다")
    @Test
    void shouldHaveConsistentTimestampsWhenResponding() {
        // given
        final LocalDateTime now = LocalDateTime.of(2025, 6, 1, 10, 0);
        final String userId = "user-001";
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "훈련", "내용", "장소",
                LocalDateTime.of(2025, 7, 1, 10, 0),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of(userId),
                5, 3, LocalDateTime.of(2025, 5, 1, 10, 0)
        );

        // CreatedScheduleEvent 소비
        final List<DomainEvent> capturedEvents = new ArrayList<>();
        schedule.publish(capturedEvents::add);
        capturedEvents.clear();

        // when
        schedule.respond(userId, AttendanceStatus.ATTENDING, "참석", now);
        schedule.publish(capturedEvents::add);

        // then
        assertThat(schedule.getUpdatedAt()).isEqualTo(now);
        final AttendanceStatusChangedEvent event = (AttendanceStatusChangedEvent) capturedEvents.get(0);
        assertThat(event.changedAt()).isEqualTo(now);
    }

    @DisplayName("일정 수정 시 일관된 시간이 사용되어야 한다")
    @Test
    void shouldUseConsistentTimeWhenUpdating() {
        // given
        final LocalDateTime now = LocalDateTime.of(2025, 6, 1, 10, 0);
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "원래 제목", "원래 내용", "원래 장소",
                LocalDateTime.of(2025, 7, 1, 10, 0),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, 3, LocalDateTime.of(2025, 5, 1, 10, 0)
        );

        final LocalDateTime newTime = LocalDateTime.of(2025, 8, 1, 10, 0);

        // when
        schedule.update("새 제목", "새 내용", "새 장소", newTime, new TrainingDetailData(), 10, 5, now);

        // then
        assertThat(schedule.getUpdatedAt()).isEqualTo(now);
    }

    // === 마감일(Deadline) 계산 테스트 ===

    @DisplayName("마감일이 지나면 isDeadlinePassed가 true를 반환해야 한다")
    @Test
    void shouldReturnTrueWhenDeadlineHasPassed() {
        // given - 일정: 2월 5일, deadlineDays: 1 → 마감일: 2월 4일
        final LocalDateTime scheduleTime = LocalDateTime.of(2025, 2, 5, 10, 0);
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "훈련", "내용", "장소",
                scheduleTime, ScheduleType.TRAINING, new TrainingDetailData(),
                List.of("user-001"), 5, 1,
                LocalDateTime.of(2025, 1, 1, 10, 0)
        );

        // when - 현재 시각: 2월 4일 12시 (마감일 이후)
        final LocalDateTime now = LocalDateTime.of(2025, 2, 4, 12, 0);

        // then
        assertThat(schedule.isDeadlinePassed(now)).isTrue();
    }

    @DisplayName("마감일 이전이면 isDeadlinePassed가 false를 반환해야 한다")
    @Test
    void shouldReturnFalseWhenBeforeDeadline() {
        // given - 일정: 2월 5일, deadlineDays: 1 → 마감일: 2월 4일
        final LocalDateTime scheduleTime = LocalDateTime.of(2025, 2, 5, 10, 0);
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "훈련", "내용", "장소",
                scheduleTime, ScheduleType.TRAINING, new TrainingDetailData(),
                List.of("user-001"), 5, 1,
                LocalDateTime.of(2025, 1, 1, 10, 0)
        );

        // when - 현재 시각: 2월 3일 (마감일 이전)
        final LocalDateTime now = LocalDateTime.of(2025, 2, 3, 10, 0);

        // then
        assertThat(schedule.isDeadlinePassed(now)).isFalse();
    }

    @DisplayName("deadlineDays가 0이면 일정 시간이 곧 마감이어야 한다")
    @Test
    void shouldUseScheduleTimeAsDeadlineWhenDeadlineDaysIsZero() {
        // given - 일정: 2월 5일 10시, deadlineDays: 0 → 마감일: 2월 5일 10시
        final LocalDateTime scheduleTime = LocalDateTime.of(2025, 2, 5, 10, 0);
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "훈련", "내용", "장소",
                scheduleTime, ScheduleType.TRAINING, new TrainingDetailData(),
                List.of("user-001"), 5, 0,
                LocalDateTime.of(2025, 1, 1, 10, 0)
        );

        // then - 2월 5일 9시 → 마감 전
        assertThat(schedule.isDeadlinePassed(LocalDateTime.of(2025, 2, 5, 9, 0))).isFalse();
        // then - 2월 5일 11시 → 마감 후
        assertThat(schedule.isDeadlinePassed(LocalDateTime.of(2025, 2, 5, 11, 0))).isTrue();
    }

    @DisplayName("출석 상태가 변경되지 않으면 이벤트가 발행되지 않아야 한다")
    @Test
    void shouldNotPublishAttendanceStatusChangedEventWhenStatusUnchanged() {
        // given
        final String userId = "user-001";
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "user-001", "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of(userId),
                5, 3, LocalDateTime.now()
        );

        // 먼저 ATTENDING으로 변경
        schedule.respond(userId, AttendanceStatus.ATTENDING, "참석");

        // 이벤트 소비
        final List<DomainEvent> capturedEvents = new ArrayList<>();
        schedule.publish(capturedEvents::add);
        capturedEvents.clear();

        // when - 같은 상태로 다시 응답
        schedule.respond(userId, AttendanceStatus.ATTENDING, "참석 재확인");
        schedule.publish(capturedEvents::add);

        // then - 상태가 변경되지 않았으므로 이벤트 발행 없음
        assertThat(capturedEvents).isEmpty();
    }
}
