package com.official.lockr.domain.club.schedule.infrastructure;

import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;
import com.official.lockr.domain.club.schedule.domain.Schedule;
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

class ScheduleRepositoryTest {

    private ScheduleRepository scheduleRepository;

    @BeforeEach
    void setUp() {
        scheduleRepository = new InMemoryScheduleRepository();
    }

    @Test
    @DisplayName("Schedule 저장 후 조회 시 동일한 데이터가 반환되어야 한다")
    void shouldReturnSameDataWhenSaveAndFind() {
        // given
        String scheduleId = "schedule-test-001";
        String clubId = "club-001";
        String title = "주간 훈련";
        String content = "정기 훈련입니다";
        String location = "축구장 A";
        LocalDateTime scheduleTime = LocalDateTime.now().plusDays(7);
        ScheduleType scheduleType = ScheduleType.TRAINING;
        TrainingDetailData detail = new TrainingDetailData();
        List<String> userIds = List.of("user-001", "user-002", "user-003");
        int minParticipants = 5;
        int maxParticipants = 20;
        int deadlineDays = 3;

        Schedule schedule = Schedule.create(
                scheduleId, clubId, title, content, location, scheduleTime,
                scheduleType, detail, userIds, minParticipants, maxParticipants, deadlineDays,
                LocalDateTime.now()
        );

        // when
        scheduleRepository.save(schedule);
        Schedule found = scheduleRepository.findById(scheduleId);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(scheduleId);
        assertThat(found.getClubId()).isEqualTo(clubId);
        assertThat(found.getTitle()).isEqualTo(title);
        assertThat(found.getContent()).isEqualTo(content);
        assertThat(found.getLocation()).isEqualTo(location);
        assertThat(found.getScheduleTime()).isEqualTo(scheduleTime);
        assertThat(found.getScheduleType()).isEqualTo(scheduleType);
        assertThat(found.getStatus()).isEqualTo(ScheduleStatus.SCHEDULED);
        assertThat(found.getMinParticipants()).isEqualTo(minParticipants);
        assertThat(found.getMaxParticipants()).isEqualTo(maxParticipants);
        assertThat(found.getDeadlineDays()).isEqualTo(deadlineDays);
        assertThat(found.getAttendances()).hasSize(3);
    }

    @Test
    @DisplayName("Attendance 변경사항이 정확히 저장되어야 한다")
    void shouldSaveAttendanceChangesCorrectly() {
        // given
        String scheduleId = "schedule-test-002";
        Schedule schedule = Schedule.create(
                scheduleId, "club-001", "훈련", "내용", "장소",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING, new TrainingDetailData(),
                List.of("user-001", "user-002"),
                5, 20, 3, LocalDateTime.now()
        );
        scheduleRepository.save(schedule);

        // when - 응답 후 다시 저장
        schedule.respond("user-001", AttendanceStatus.ATTENDING, "참석합니다");
        scheduleRepository.save(schedule);
        Schedule found = scheduleRepository.findById(scheduleId);

        // then
        assertThat(found.getAttendances())
                .filteredOn(a -> a.getUserId().equals("user-001"))
                .allMatch(a -> a.getStatus() == AttendanceStatus.ATTENDING);
    }

    @Test
    @DisplayName("존재하지 않는 Schedule 조회 시 null 반환")
    void shouldReturnNullWhenScheduleNotFound() {
        // when
        Schedule found = scheduleRepository.findById("non-existent-id");

        // then
        assertThat(found).isNull();
    }
}
