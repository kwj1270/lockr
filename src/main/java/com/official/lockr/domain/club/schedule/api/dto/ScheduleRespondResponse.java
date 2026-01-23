package com.official.lockr.domain.club.schedule.api.dto;

import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;
import com.official.lockr.domain.club.schedule.domain.Schedule;
import com.official.lockr.domain.club.schedule.domain.ScheduleStatus;
import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.official.lockr.domain.club.schedule.domain.vo.ScheduleDetailData;
import org.jooq.Configuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jooq.generated.tables.UserAdditionalInfoJOOQEntity.USER_ADDITIONAL_INFO;
import static org.jooq.impl.DSL.using;

public record ScheduleRespondResponse(
        String id,
        String clubId,
        String title,
        String content,
        String location,
        LocalDateTime scheduleTime,
        ScheduleType scheduleType,
        ScheduleDetailData detail,
        List<AttendanceResponse> attendances,
        ScheduleStatus status,
        int attendingCount,
        int notAttendingCount,
        int noResponseCount,
        AttendanceStatus myAttendanceStatus,  // 현재 사용자의 참석 상태 추가
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * currentUserId를 받아서 해당 사용자의 참석 상태를 포함한 응답 생성
     */
    public static ScheduleRespondResponse from(final Schedule schedule, final String currentUserId, final Configuration configuration) {
        // attendances에서 현재 사용자의 상태 찾기
        final AttendanceStatus myStatus = schedule.getAttendances().stream()
                .filter(attendance -> attendance.getUserId().equals(currentUserId))
                .map(attendance -> attendance.getStatus())
                .findFirst()
                .orElse(AttendanceStatus.NO_RESPONSE);

        // 모든 userId 추출
        final List<String> userIds = schedule.getAttendances().stream()
                .map(attendance -> attendance.getUserId())
                .distinct()
                .toList();

        // 사용자 이름 조회
        final Map<String, String> userNames = userIds.isEmpty() ? Map.of() : using(configuration)
                .select(USER_ADDITIONAL_INFO.USER_ID, USER_ADDITIONAL_INFO.NAME)
                .from(USER_ADDITIONAL_INFO)
                .where(USER_ADDITIONAL_INFO.USER_ID.in(userIds))
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        record -> record.get(USER_ADDITIONAL_INFO.USER_ID),
                        record -> record.get(USER_ADDITIONAL_INFO.NAME)
                ));

        return new ScheduleRespondResponse(
                schedule.getId(),
                schedule.getClubId(),
                schedule.getTitle(),
                schedule.getContent(),
                schedule.getLocation(),
                schedule.getScheduleTime(),
                schedule.getScheduleType(),
                schedule.getDetail(),
                schedule.getAttendances().stream()
                        .map(attendance -> AttendanceResponse.from(attendance))
                        .toList(),
                schedule.getStatus(),
                schedule.getAttendingCount(),
                schedule.getNotAttendingCount(),
                schedule.getNoResponseCount(),
                myStatus,
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
        );
    }
}


