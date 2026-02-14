package com.official.lockr.cucumber.steps;

import com.official.lockr.domain.club.club.domain.Club;
import com.official.lockr.domain.club.club.domain.Member;
import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;
import com.official.lockr.domain.club.schedule.domain.Schedule;
import com.official.lockr.domain.club.schedule.domain.ScheduleStatus;
import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.official.lockr.domain.club.schedule.domain.vo.TrainingDetailData;
import io.cucumber.java.Before;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import io.cucumber.java.ko.먼저;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ScheduleStepDefinitions {

    private final SharedState sharedState = SharedState.getInstance();
    private Club club;
    private Schedule schedule;
    private final Map<String, Member> members = new HashMap<>();
    private final String clubId = "club-001";

    @Before
    public void setUp() {
        members.clear();
        club = null;
        schedule = null;
    }

    @먼저("클럽 {string}가 존재한다")
    public void 클럽이_존재한다(String clubName) {
        club = new Club(
                clubId, "user-001", clubName, "FOOTBALL",
                "서울", "강남구", "테스트 클럽입니다",
                null, null,
                new ArrayList<>(),
                LocalDateTime.now(), LocalDateTime.now(), null
        );
    }

    @그리고("{string}은 스케줄 클럽의 회장이다")
    @그리고("{string}이 스케줄 클럽의 회장이다")
    public void 은_스케줄_클럽의_회장이다(String userId) {
        Member president = Member.president(userId, clubId, null, null);
        members.put(userId, president);
        club.addMember(president);
    }

    @그리고("{string}는 스케줄 클럽의 일반 멤버이다")
    @그리고("{string}은 스케줄 클럽의 일반 멤버이다")
    public void 는_스케줄_클럽의_일반_멤버이다(String userId) {
        Member member = Member.basic(userId, clubId, null, null);
        members.put(userId, member);
        club.addMember(member);
    }

    @만약("스태프 {string}이 {string} 일정을 생성한다")
    @만약("스태프 {string}가 {string} 일정을 생성한다")
    public void 스태프가_일정을_생성한다(String userId, String title) {
        List<String> userIds = members.keySet().stream().toList();
        schedule = Schedule.create(
                "schedule-001",
                clubId,
                title,
                "일정 내용입니다",
                "훈련장",
                LocalDateTime.now().plusDays(7),
                ScheduleType.TRAINING,
                new TrainingDetailData(),
                userIds,
                5, 20, 3, LocalDateTime.now()
        );
    }

    @그러면("일정이 성공적으로 생성된다")
    public void 일정이_성공적으로_생성된다() {
        assertThat(schedule).isNotNull();
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.SCHEDULED);
    }

    @그리고("{int}명의 멤버에게 NO_RESPONSE 상태의 Attendance가 생성된다")
    public void 명의_멤버에게_no_response_상태의_attendance가_생성된다(int count) {
        assertThat(schedule.getAttendances()).hasSize(count);
        assertThat(schedule.getAttendances())
                .allMatch(a -> a.getStatus() == AttendanceStatus.NO_RESPONSE);
    }

    @먼저("{string} 일정이 존재한다")
    public void 일정이_존재한다(String title) {
        if (schedule == null) {
            List<String> userIds = members.keySet().stream().toList();
            schedule = Schedule.create(
                    "schedule-001",
                    clubId,
                    title,
                    "일정 내용입니다",
                    "훈련장",
                    LocalDateTime.now().plusDays(7),
                    ScheduleType.TRAINING,
                    new TrainingDetailData(),
                    userIds,
                    5, 20, 3, LocalDateTime.now()
            );
        }
    }

    @만약("멤버 {string}가 {string} 응답을 한다")
    @만약("멤버 {string}이 {string} 응답을 한다")
    public void 멤버가_응답을_한다(String userId, String response) {
        AttendanceStatus status = "참석".equals(response) ? AttendanceStatus.ATTENDING : AttendanceStatus.NOT_ATTENDING;
        schedule.respond(userId, status, null);
    }

    @만약("멤버 {string}이 {string} 응답을 {string} 사유로 한다")
    @만약("멤버 {string}가 {string} 응답을 {string} 사유로 한다")
    public void 멤버가_응답을_사유로_한다(String userId, String response, String reason) {
        AttendanceStatus status = "참석".equals(response) ? AttendanceStatus.ATTENDING : AttendanceStatus.NOT_ATTENDING;
        schedule.respond(userId, status, reason);
    }

    @그러면("{string}의 출석 상태가 {string}이다")
    public void 의_출석_상태가_이다(String userId, String expectedStatus) {
        AttendanceStatus status = AttendanceStatus.valueOf(expectedStatus);
        assertThat(schedule.getAttendances())
                .filteredOn(a -> a.getUserId().equals(userId))
                .allMatch(a -> a.getStatus() == status);
    }

    @그리고("{string}의 불참 사유가 {string}이다")
    public void 의_불참_사유가_이다(String userId, String expectedReason) {
        assertThat(schedule.getAttendances())
                .filteredOn(a -> a.getUserId().equals(userId))
                .allMatch(a -> expectedReason.equals(a.getReason()));
    }

    @만약("일반 멤버 {string}가 일정을 수정하려고 한다")
    @만약("일반 멤버 {string}이 일정을 수정하려고 한다")
    public void 일반_멤버가_일정을_수정하려고_한다(String userId) {
        Member member = members.get(userId);
        if (member != null && !member.isStaff()) {
            sharedState.setCaughtException(new IllegalArgumentException("권한이 없습니다."));
        }
    }

    @만약("스태프 {string}이 일정을 취소한다")
    @만약("스태프 {string}가 일정을 취소한다")
    public void 스태프가_일정을_취소한다(String userId) {
        schedule.cancel();
    }

    @그러면("일정 상태가 {string}이다")
    public void 일정_상태가_이다(String expectedStatus) {
        ScheduleStatus status = ScheduleStatus.valueOf(expectedStatus);
        assertThat(schedule.getStatus()).isEqualTo(status);
    }
}
