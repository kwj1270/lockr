package com.official.lockr.domain.club.schedule.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.schedule.api.dto.*;
import jakarta.servlet.http.HttpSession;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.SchedulesDao;
import org.jooq.impl.DSL;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static java.util.Objects.isNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jooq.generated.tables.AttendancesJOOQEntity.ATTENDANCES;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;
import static org.jooq.generated.tables.SchedulesJOOQEntity.SCHEDULES;
import static org.jooq.generated.tables.UserAdditionalInfoJOOQEntity.USER_ADDITIONAL_INFO;

@RequestMapping("/api/v1/clubs/{clubId}/schedules")
@RestController
public class ScheduleClubsQueryApi {

    private final SchedulesDao schedulesDao;

    public ScheduleClubsQueryApi(final Configuration configuration) {
        this.schedulesDao = new SchedulesDao(configuration);
    }

    @GetMapping
    public ResponseEntity<SchedulesResponse> getSchedules(
            @PathVariable String clubId,
            final HttpSession httpSession,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "from", required = false) LocalDateTime from,
            @RequestParam(value = "to", required = false) LocalDateTime to,
            @RequestParam(value = "type", required = false) String type
    ) {
        // Get current user from session
        final SignInSession signIn = session(httpSession);

        // Check if user is a member of the club
        final boolean isMember = schedulesDao.ctx()
                .fetchExists(
                        schedulesDao.ctx()
                                .selectOne()
                                .from(MEMBERS)
                                .where(MEMBERS.CLUB_ID.eq(clubId))
                                .and(MEMBERS.USER_ID.eq(signIn.userId()))
                                .and(MEMBERS.DELETED_AT.isNull())
                );

        if (!isMember) {
            return ResponseEntity.status(403).build();
        }

        // Build conditions
        List<Condition> conditions = new ArrayList<>();
        conditions.add(SCHEDULES.CLUB_ID.eq(clubId));
        conditions.add(SCHEDULES.DELETED_AT.isNull());

        if (status != null && !status.isEmpty()) {
            conditions.add(SCHEDULES.STATUS.eq(status));
        }
        if (from != null) {
            conditions.add(SCHEDULES.SCHEDULE_TIME.greaterOrEqual(from));
        }
        if (to != null) {
            conditions.add(SCHEDULES.SCHEDULE_TIME.lessOrEqual(to));
        }
        if (type != null && !type.isEmpty()) {
            conditions.add(SCHEDULES.TYPE.eq(type));
        }

        // Step 1: Get schedule IDs with filtering
        final var scheduleIds = schedulesDao.ctx()
                .select(SCHEDULES.ID)
                .from(SCHEDULES)
                .where(conditions)
                .orderBy(SCHEDULES.SCHEDULE_TIME.desc())
                .fetch()
                .map(record -> record.get(SCHEDULES.ID));

        if (scheduleIds.isEmpty()) {
            return ResponseEntity.ok(new SchedulesResponse(List.of()));
        }

        // Step 2: Get attendance counts per schedule (in-memory aggregation)
        final Map<String, Map<String, Integer>> attendanceCounts = fetchAttendanceCounts(scheduleIds);

        // Step 3: Fetch schedule data
        final List<ScheduleItemResponse> schedules = schedulesDao.ctx()
                .select(
                        SCHEDULES.ID,
                        SCHEDULES.CLUB_ID,
                        SCHEDULES.TITLE,
                        SCHEDULES.CONTENT,
                        SCHEDULES.LOCATION,
                        SCHEDULES.SCHEDULE_TIME,
                        SCHEDULES.TYPE,
                        SCHEDULES.STATUS,
                        SCHEDULES.CREATED_AT,
                        SCHEDULES.UPDATED_AT
                )
                .from(SCHEDULES)
                .where(SCHEDULES.ID.in(scheduleIds))
                .orderBy(SCHEDULES.SCHEDULE_TIME.desc())
                .fetch()
                .map(record -> {
                    final String scheduleId = record.get(SCHEDULES.ID);
                    final Map<String, Integer> counts = attendanceCounts.getOrDefault(scheduleId, Map.of());
                    return new ScheduleItemResponse(
                            scheduleId,
                            record.get(SCHEDULES.CLUB_ID),
                            record.get(SCHEDULES.TITLE),
                            record.get(SCHEDULES.CONTENT),
                            record.get(SCHEDULES.LOCATION),
                            record.get(SCHEDULES.SCHEDULE_TIME),
                            record.get(SCHEDULES.TYPE),
                            record.get(SCHEDULES.STATUS),
                            counts.getOrDefault("ATTENDING", 0),
                            counts.getOrDefault("NOT_ATTENDING", 0),
                            counts.getOrDefault("NO_RESPONSE", 0),
                            record.get(SCHEDULES.CREATED_AT),
                            record.get(SCHEDULES.UPDATED_AT)
                    );
                });

        return ResponseEntity.ok(new SchedulesResponse(schedules));
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDetailResponse> getSchedule(
            @PathVariable String clubId,
            @PathVariable String scheduleId,
            final HttpSession httpSession
    ) {
        final SignInSession signIn = session(httpSession);

        final boolean isMember = schedulesDao.ctx()
                .fetchExists(
                        schedulesDao.ctx()
                                .selectOne()
                                .from(MEMBERS)
                                .where(MEMBERS.CLUB_ID.eq(clubId))
                                .and(MEMBERS.USER_ID.eq(signIn.userId()))
                                .and(MEMBERS.DELETED_AT.isNull())
                );

        if (!isMember) {
            return ResponseEntity.status(403).build();
        }

        // Fetch schedule
        final var scheduleRecord = schedulesDao.ctx()
                .selectFrom(SCHEDULES)
                .where(SCHEDULES.ID.eq(scheduleId))
                .and(SCHEDULES.CLUB_ID.eq(clubId))
                .and(SCHEDULES.DELETED_AT.isNull())
                .fetchOne();

        if (scheduleRecord == null) {
            return ResponseEntity.status(404).build();
        }

        // Fetch attendances with user info
        final List<AttendanceItemResponse> attendances = schedulesDao.ctx()
                .select(
                        ATTENDANCES.ID,
                        ATTENDANCES.USER_ID,
                        USER_ADDITIONAL_INFO.NAME.as("user_name"),
                        ATTENDANCES.STATUS,
                        ATTENDANCES.REASON
                )
                .from(ATTENDANCES)
                .leftJoin(USER_ADDITIONAL_INFO).on(USER_ADDITIONAL_INFO.USER_ID.eq(ATTENDANCES.USER_ID))
                .where(ATTENDANCES.SCHEDULE_ID.eq(scheduleId))
                .and(ATTENDANCES.DELETED_AT.isNull())
                .fetch()
                .map(record -> new AttendanceItemResponse(
                        record.get(ATTENDANCES.ID),
                        record.get(ATTENDANCES.USER_ID),
                        record.get("user_name", String.class),
                        record.get(ATTENDANCES.STATUS),
                        record.get(ATTENDANCES.REASON)
                ));

        // Calculate attendance counts
        final int attendingCount = (int) attendances.stream()
                .filter(a -> "ATTENDING".equals(a.status()))
                .count();
        final int notAttendingCount = (int) attendances.stream()
                .filter(a -> "NOT_ATTENDING".equals(a.status()))
                .count();
        final int noResponseCount = (int) attendances.stream()
                .filter(a -> "NO_RESPONSE".equals(a.status()))
                .count();

        // Find current user's attendance status
        final String myAttendanceStatus = attendances.stream()
                .filter(a -> signIn.userId().equals(a.userId()))
                .findFirst()
                .map(AttendanceItemResponse::status)
                .orElse("NO_RESPONSE");

        final String detailData = scheduleRecord.get(SCHEDULES.DETAIL_DATA) != null
                ? scheduleRecord.get(SCHEDULES.DETAIL_DATA).toString()
                : null;

        final ScheduleDetailResponse response = new ScheduleDetailResponse(
                scheduleRecord.get(SCHEDULES.ID),
                scheduleRecord.get(SCHEDULES.CLUB_ID),
                scheduleRecord.get(SCHEDULES.TITLE),
                scheduleRecord.get(SCHEDULES.CONTENT),
                ScheduleLocationResponse.from(scheduleRecord.get(SCHEDULES.LOCATION)),
                scheduleRecord.get(SCHEDULES.SCHEDULE_TIME),
                scheduleRecord.get(SCHEDULES.TYPE),
                detailData,
                scheduleRecord.get(SCHEDULES.STATUS),
                scheduleRecord.get(SCHEDULES.MIN_PARTICIPANTS),
                scheduleRecord.get(SCHEDULES.MAX_PARTICIPANTS),
                scheduleRecord.get(SCHEDULES.DEADLINE_DAYS),
                attendances,
                attendingCount,
                notAttendingCount,
                noResponseCount,
                myAttendanceStatus,
                scheduleRecord.get(SCHEDULES.CREATED_AT),
                scheduleRecord.get(SCHEDULES.UPDATED_AT)
        );
        return ResponseEntity.ok(response);
    }

    private Map<String, Map<String, Integer>> fetchAttendanceCounts(List<String> scheduleIds) {
        return schedulesDao.ctx()
                .select(
                        ATTENDANCES.SCHEDULE_ID,
                        ATTENDANCES.STATUS,
                        DSL.count().as("count")
                )
                .from(ATTENDANCES)
                .where(ATTENDANCES.SCHEDULE_ID.in(scheduleIds))
                .and(ATTENDANCES.DELETED_AT.isNull())
                .groupBy(ATTENDANCES.SCHEDULE_ID, ATTENDANCES.STATUS)
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        record -> record.get(ATTENDANCES.SCHEDULE_ID),
                        Collectors.toMap(
                                record -> record.get(ATTENDANCES.STATUS),
                                record -> record.get("count", Integer.class)
                        )
                ));
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(signIn)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return signIn;
    }
}
