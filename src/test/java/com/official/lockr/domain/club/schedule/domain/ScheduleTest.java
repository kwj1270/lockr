package com.official.lockr.domain.club.schedule.domain;

import com.official.lockr.domain.club.schedule.domain.event.CancelledScheduleEvent;
import com.official.lockr.domain.club.schedule.domain.event.CreatedScheduleEvent;
import com.official.lockr.domain.club.schedule.domain.event.RespondedToScheduleEvent;
import com.official.lockr.domain.club.schedule.domain.event.UpdatedScheduleEvent;
import com.official.lockr.domain.club.schedule.domain.vo.MatchDetailData;
import com.official.lockr.domain.club.schedule.domain.vo.SocialDetailData;
import com.official.lockr.domain.club.schedule.domain.vo.TrainingDetailData;
import com.official.lockr.global.ddd.DomainEvent;
import com.official.lockr.global.ddd.DomainEventPublisher;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScheduleTest {

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
        final int maxParticipants = 20;
        final int deadlineDays = 3;

        // when
        final Schedule schedule = Schedule.create(
                id, clubId, title, content, location, scheduleTime,
                scheduleType, detail, userIds, minParticipants, maxParticipants, deadlineDays
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
        assertThat(schedule.getMaxParticipants()).isEqualTo(maxParticipants);
        assertThat(schedule.getDeadlineDays()).isEqualTo(deadlineDays);
        assertThat(schedule.getAttendances()).hasSize(2);
        assertThat(schedule.getCreatedAt()).isNotNull();
        assertThat(schedule.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenScheduleTimeIsInThePast() {
        // given
        final LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        // when & then
        assertThatThrownBy(() -> Schedule.create(
                "schedule-001", "club-001", "과거 일정", "내용", "장소", pastTime,
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, 20, 3
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
    }

    @Test
    void shouldPublishCreatedScheduleEventWhenCreatingSchedule() {
        // given
        final String id = "schedule-001";
        final String clubId = "club-001";
        final LocalDateTime scheduleTime = LocalDateTime.now().plusDays(7);
        final ScheduleType scheduleType = ScheduleType.TRAINING;

        final Schedule schedule = Schedule.create(
                id, clubId, "훈련", "내용", "장소", scheduleTime,
                scheduleType, new TrainingDetailData(), List.of("user-001"),
                5, 20, 3
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

    @Test
    void shouldSetMatchDetailDataWhenCreatingMatchTypeSchedule() {
        // given
        final String homeClubId = "club-001";
        final String awayClubId = "club-002";
        final String opponentName = "상대팀FC";
        final MatchDetailData matchDetail = new MatchDetailData(homeClubId, awayClubId, opponentName);

        // when
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "정기전", "내용", "경기장",
                LocalDateTime.now().plusDays(7),
                ScheduleType.MATCH, matchDetail, List.of("user-001"),
                11, 22, 3
        );

        // then
        assertThat(schedule.getScheduleType()).isEqualTo(ScheduleType.MATCH);
        assertThat(schedule.getDetail()).isInstanceOf(MatchDetailData.class);

        final MatchDetailData detail = (MatchDetailData) schedule.getDetail();
        assertThat(detail.homeClubId()).isEqualTo(homeClubId);
        assertThat(detail.awayClubId()).isEqualTo(awayClubId);
        assertThat(detail.opponentName()).isEqualTo(opponentName);
    }

    @Test
    void shouldSetTrainingDetailDataWhenCreatingTrainingTypeSchedule() {
        // given
        final TrainingDetailData trainingDetail = new TrainingDetailData();

        // when
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "정기 훈련", "체력 훈련", "훈련장",
                LocalDateTime.now().plusDays(3),
                ScheduleType.TRAINING, trainingDetail, List.of("user-001"),
                5, 15, 2
        );

        // then
        assertThat(schedule.getScheduleType()).isEqualTo(ScheduleType.TRAINING);
        assertThat(schedule.getDetail()).isInstanceOf(TrainingDetailData.class);
    }

    @Test
    void shouldSetSocialDetailDataWhenCreatingSocialEventTypeSchedule() {
        // given
        final SocialDetailData socialDetail = new SocialDetailData();

        // when
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "회식", "시즌 종료 회식", "레스토랑",
                LocalDateTime.now().plusDays(14),
                ScheduleType.SOCIAL_EVENT, socialDetail, List.of("user-001", "user-002"),
                10, 30, 5
        );

        // then
        assertThat(schedule.getScheduleType()).isEqualTo(ScheduleType.SOCIAL_EVENT);
        assertThat(schedule.getDetail()).isInstanceOf(SocialDetailData.class);
    }

    @Test
    void shouldUpdateAttendanceStatusWhenRespondingToSchedule() {
        // given
        final String userId = "user-001";
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of(userId),
                5, 20, 3
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

    @Test
    void shouldUpdateStatusWhenUserRespondsAgain() {
        // given
        final String userId = "user-001";
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of(userId),
                5, 20, 3
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

    @Test
    void shouldPublishRespondedToScheduleEventWhenResponding() {
        // given
        final String scheduleId = "schedule-001";
        final String clubId = "club-001";
        final String userId = "user-001";
        final Schedule schedule = Schedule.create(
                scheduleId, clubId, "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of(userId),
                5, 20, 3
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
        assertThat(capturedEvents.get(0)).isInstanceOf(RespondedToScheduleEvent.class);

        final RespondedToScheduleEvent event = (RespondedToScheduleEvent) capturedEvents.get(0);
        assertThat(event.scheduleId()).isEqualTo(scheduleId);
        assertThat(event.clubId()).isEqualTo(clubId);
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.previousStatus()).isEqualTo(AttendanceStatus.NO_RESPONSE);
        assertThat(event.newStatus()).isEqualTo(AttendanceStatus.ATTENDING);
    }

    @Test
    void shouldThrowExceptionWhenRespondingToCancelledSchedule() {
        // given
        final String userId = "user-001";
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of(userId),
                5, 20, 3
        );

        schedule.cancel();

        // when & then
        assertThatThrownBy(() -> schedule.respond(userId, AttendanceStatus.ATTENDING, "참석"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cancelled");
    }

    @Test
    void shouldUpdateFieldsWhenUpdatingSchedule() {
        // given
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "원래 제목", "원래 내용", "원래 장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, 20, 3
        );

        final String newTitle = "변경된 제목";
        final String newContent = "변경된 내용";
        final String newLocation = "변경된 장소";
        final LocalDateTime newTime = LocalDateTime.now().plusDays(14);
        final TrainingDetailData newDetail = new TrainingDetailData();

        // when
        schedule.update(newTitle, newContent, newLocation, newTime, newDetail, 10, 25, 5);

        // then
        assertThat(schedule.getTitle()).isEqualTo(newTitle);
        assertThat(schedule.getContent()).isEqualTo(newContent);
        assertThat(schedule.getLocation()).isEqualTo(newLocation);
        assertThat(schedule.getScheduleTime()).isEqualTo(newTime);
        assertThat(schedule.getMinParticipants()).isEqualTo(10);
        assertThat(schedule.getMaxParticipants()).isEqualTo(25);
        assertThat(schedule.getDeadlineDays()).isEqualTo(5);
    }

    @Test
    void shouldPublishUpdatedScheduleEventWhenUpdating() {
        // given
        final String scheduleId = "schedule-001";
        final String clubId = "club-001";
        final Schedule schedule = Schedule.create(
                scheduleId, clubId, "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, 20, 3
        );

        final List<DomainEvent> capturedEvents = new ArrayList<>();
        schedule.publish(capturedEvents::add);
        capturedEvents.clear();

        final LocalDateTime newTime = LocalDateTime.now().plusDays(14);

        // when
        schedule.update("새 제목", "새 내용", "새 장소", newTime, new TrainingDetailData(), 10, 25, 5);
        schedule.publish(capturedEvents::add);

        // then
        assertThat(capturedEvents).hasSize(1);
        assertThat(capturedEvents.get(0)).isInstanceOf(UpdatedScheduleEvent.class);

        final UpdatedScheduleEvent event = (UpdatedScheduleEvent) capturedEvents.get(0);
        assertThat(event.scheduleId()).isEqualTo(scheduleId);
        assertThat(event.clubId()).isEqualTo(clubId);
        assertThat(event.newScheduleTime()).isEqualTo(newTime);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingCancelledSchedule() {
        // given
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, 20, 3
        );

        schedule.cancel();

        // when & then
        assertThatThrownBy(() -> schedule.update(
                "새 제목", "새 내용", "새 장소",
                LocalDateTime.now().plusDays(14),
                new TrainingDetailData(), 10, 25, 5
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cancelled");
    }

    @Test
    void shouldChangeStatusToCancelledWhenCancelling() {
        // given
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, 20, 3
        );

        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.SCHEDULED);

        // when
        schedule.cancel();

        // then
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.CANCELLED);
        assertThat(schedule.isCancelled()).isTrue();
        assertThat(schedule.getDeletedAt()).isNotNull();
    }

    @Test
    void shouldPublishCancelledScheduleEventWhenCancelling() {
        // given
        final String scheduleId = "schedule-001";
        final String clubId = "club-001";
        final ScheduleType scheduleType = ScheduleType.TRAINING;
        final Schedule schedule = Schedule.create(
                scheduleId, clubId, "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                scheduleType, new TrainingDetailData(), List.of("user-001"),
                5, 20, 3
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

    @Test
    void shouldThrowExceptionWhenCancellingAlreadyCancelledSchedule() {
        // given
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, 20, 3
        );

        schedule.cancel();

        // when & then
        assertThatThrownBy(schedule::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cancelled");
    }

    @Test
    void shouldCreateNoResponseAttendanceWhenAddingMember() {
        // given
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(), List.of("user-001"),
                5, 20, 3
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

    @Test
    void shouldCountAttendingMembersCorrectly() {
        // given
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(),
                List.of("user-001", "user-002", "user-003"),
                5, 20, 3
        );

        schedule.respond("user-001", AttendanceStatus.ATTENDING, null);
        schedule.respond("user-002", AttendanceStatus.ATTENDING, null);
        schedule.respond("user-003", AttendanceStatus.NOT_ATTENDING, "불참");

        // when & then
        assertThat(schedule.getAttendingCount()).isEqualTo(2);
    }

    @Test
    void shouldCountNotAttendingMembersCorrectly() {
        // given
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(),
                List.of("user-001", "user-002", "user-003", "user-004"),
                5, 20, 3
        );

        schedule.respond("user-001", AttendanceStatus.ATTENDING, null);
        schedule.respond("user-002", AttendanceStatus.NOT_ATTENDING, "출장");
        schedule.respond("user-003", AttendanceStatus.NOT_ATTENDING, "부상");

        // when & then
        assertThat(schedule.getNotAttendingCount()).isEqualTo(2);
    }

    @Test
    void shouldCountNoResponseMembersCorrectly() {
        // given
        final Schedule schedule = Schedule.create(
                "schedule-001", "club-001", "제목", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(),
                List.of("user-001", "user-002", "user-003", "user-004", "user-005"),
                5, 20, 3
        );

        schedule.respond("user-001", AttendanceStatus.ATTENDING, null);
        schedule.respond("user-002", AttendanceStatus.NOT_ATTENDING, "출장");
        // user-003, user-004, user-005는 미응답

        // when & then
        assertThat(schedule.getNoResponseCount()).isEqualTo(3);
    }
}
