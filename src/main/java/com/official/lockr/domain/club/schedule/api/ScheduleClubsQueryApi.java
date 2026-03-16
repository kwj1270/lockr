package com.official.lockr.domain.club.schedule.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.schedule.api.dto.*;
import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;
import com.official.lockr.domain.club.schedule.domain.ScheduleStatus;
import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.generated.tables.daos.SchedulesDao;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
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

    private static final Table<?> SCHEDULE_COMMENTS = DSL.table("schedule_comments");
    private static final Field<String> SC_ID = DSL.field(DSL.name("schedule_comments", "id"), SQLDataType.VARCHAR);
    private static final Field<String> SC_SCHEDULE_ID = DSL.field(DSL.name("schedule_comments", "schedule_id"), SQLDataType.VARCHAR);
    private static final Field<String> SC_USER_ID = DSL.field(DSL.name("schedule_comments", "user_id"), SQLDataType.VARCHAR);
    private static final Field<String> SC_CONTENT = DSL.field(DSL.name("schedule_comments", "content"), SQLDataType.VARCHAR);
    private static final Field<LocalDateTime> SC_CREATED_AT = DSL.field(DSL.name("schedule_comments", "created_at"), SQLDataType.LOCALDATETIME);
    private static final Field<LocalDateTime> SC_UPDATED_AT = DSL.field(DSL.name("schedule_comments", "updated_at"), SQLDataType.LOCALDATETIME);
    private static final Field<LocalDateTime> SC_DELETED_AT = DSL.field(DSL.name("schedule_comments", "deleted_at"), SQLDataType.LOCALDATETIME);

    private final SchedulesDao schedulesDao;
    private final ObjectMapper objectMapper;

    public ScheduleClubsQueryApi(final Configuration configuration, final ObjectMapper objectMapper) {
        this.schedulesDao = new SchedulesDao(configuration);
        this.objectMapper = objectMapper;
    }

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    @GetMapping
    public ResponseEntity<SchedulesResponse> getSchedules(
            @PathVariable String clubId,
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @RequestParam(value = "status", required = false) ScheduleStatus status,
            @RequestParam(value = "from", required = false) LocalDateTime from,
            @RequestParam(value = "to", required = false) LocalDateTime to,
            @RequestParam(value = "type", required = false) ScheduleType type,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "offset", required = false) Integer offset
    ) {
        // Check if user is a member of the club
        final boolean isMember = schedulesDao.ctx()
                .fetchExists(
                        schedulesDao.ctx()
                                .selectOne()
                                .from(MEMBERS)
                                .where(MEMBERS.CLUB_ID.eq(clubId))
                                .and(MEMBERS.USER_ID.eq(signInSession.userId()))
                                .and(MEMBERS.DELETED_AT.isNull())
                );

        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a member of this club");
        }

        // Build conditions
        List<Condition> conditions = new ArrayList<>();
        conditions.add(SCHEDULES.CLUB_ID.eq(clubId));
        conditions.add(SCHEDULES.DELETED_AT.isNull());

        if (status != null) {
            conditions.add(SCHEDULES.STATUS.eq(status.name()));
        }
        if (from != null) {
            conditions.add(SCHEDULES.SCHEDULE_TIME.greaterOrEqual(from));
        }
        if (to != null) {
            conditions.add(SCHEDULES.SCHEDULE_TIME.lessOrEqual(to));
        }
        if (type != null) {
            conditions.add(SCHEDULES.TYPE.eq(type.name()));
        }

        // Step 1: Get schedule IDs with filtering and pagination
        final int effectiveLimit = Math.min(limit != null && limit > 0 ? limit : DEFAULT_LIMIT, MAX_LIMIT);
        final int effectiveOffset = offset != null && offset >= 0 ? offset : 0;

        final var scheduleIds = schedulesDao.ctx()
                .select(SCHEDULES.ID)
                .from(SCHEDULES)
                .where(conditions)
                .orderBy(SCHEDULES.SCHEDULE_TIME.desc())
                .limit(effectiveLimit)
                .offset(effectiveOffset)
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
                            ScheduleLocationResponse.from(record.get(SCHEDULES.LOCATION), objectMapper),
                            record.get(SCHEDULES.SCHEDULE_TIME),
                            ScheduleType.valueOf(record.get(SCHEDULES.TYPE)),
                            ScheduleStatus.valueOf(record.get(SCHEDULES.STATUS)),
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
            @RequestAttribute("signInSession") final SignInSession signInSession
    ) {
        final boolean isMember = schedulesDao.ctx()
                .fetchExists(
                        schedulesDao.ctx()
                                .selectOne()
                                .from(MEMBERS)
                                .where(MEMBERS.CLUB_ID.eq(clubId))
                                .and(MEMBERS.USER_ID.eq(signInSession.userId()))
                                .and(MEMBERS.DELETED_AT.isNull())
                );

        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a member of this club");
        }

        // Fetch schedule
        final var scheduleRecord = schedulesDao.ctx()
                .selectFrom(SCHEDULES)
                .where(SCHEDULES.ID.eq(scheduleId))
                .and(SCHEDULES.CLUB_ID.eq(clubId))
                .and(SCHEDULES.DELETED_AT.isNull())
                .fetchOne();

        if (scheduleRecord == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found");
        }

        // Fetch attendances with user info and member profile image
        final List<AttendanceItemResponse> attendances = schedulesDao.ctx()
                .select(
                        ATTENDANCES.ID,
                        ATTENDANCES.USER_ID,
                        USER_ADDITIONAL_INFO.NAME.as("user_name"),
                        ATTENDANCES.STATUS,
                        ATTENDANCES.REASON,
                        MEMBERS.PROFILE_IMAGE
                )
                .from(ATTENDANCES)
                .leftJoin(USER_ADDITIONAL_INFO).on(USER_ADDITIONAL_INFO.USER_ID.eq(ATTENDANCES.USER_ID))
                .leftJoin(MEMBERS).on(MEMBERS.USER_ID.eq(ATTENDANCES.USER_ID)
                        .and(MEMBERS.CLUB_ID.eq(clubId))
                        .and(MEMBERS.DELETED_AT.isNull()))
                .where(ATTENDANCES.SCHEDULE_ID.eq(scheduleId))
                .and(ATTENDANCES.DELETED_AT.isNull())
                .fetch()
                .map(record -> new AttendanceItemResponse(
                        record.get(ATTENDANCES.ID),
                        record.get(ATTENDANCES.USER_ID),
                        record.get("user_name", String.class),
                        AttendanceStatus.valueOf(record.get(ATTENDANCES.STATUS)),
                        record.get(ATTENDANCES.REASON),
                        record.get(MEMBERS.PROFILE_IMAGE)
                ));

        // Calculate attendance counts
        final int attendingCount = (int) attendances.stream()
                .filter(a -> AttendanceStatus.ATTENDING == a.status())
                .count();
        final int notAttendingCount = (int) attendances.stream()
                .filter(a -> AttendanceStatus.NOT_ATTENDING == a.status())
                .count();
        final int noResponseCount = (int) attendances.stream()
                .filter(a -> AttendanceStatus.NO_RESPONSE == a.status())
                .count();

        // Find current user's attendance status
        final AttendanceStatus myAttendanceStatus = attendances.stream()
                .filter(a -> signInSession.userId().equals(a.userId()))
                .findFirst()
                .map(AttendanceItemResponse::status)
                .orElse(AttendanceStatus.NO_RESPONSE);

        final String detailData = scheduleRecord.get(SCHEDULES.DETAIL_DATA) != null
                ? scheduleRecord.get(SCHEDULES.DETAIL_DATA).toString()
                : null;

        final ScheduleDetailResponse response = new ScheduleDetailResponse(
                scheduleRecord.get(SCHEDULES.ID),
                scheduleRecord.get(SCHEDULES.CLUB_ID),
                scheduleRecord.get(SCHEDULES.TITLE),
                scheduleRecord.get(SCHEDULES.CONTENT),
                ScheduleLocationResponse.from(scheduleRecord.get(SCHEDULES.LOCATION), objectMapper),
                scheduleRecord.get(SCHEDULES.SCHEDULE_TIME),
                ScheduleType.valueOf(scheduleRecord.get(SCHEDULES.TYPE)),
                detailData,
                ScheduleStatus.valueOf(scheduleRecord.get(SCHEDULES.STATUS)),
                scheduleRecord.get(SCHEDULES.MIN_PARTICIPANTS),
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

    @GetMapping("/{scheduleId}/comments")
    public ResponseEntity<ScheduleCommentsResponse> getScheduleComments(
            @PathVariable String clubId,
            @PathVariable String scheduleId,
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @RequestParam(value = "cursor", required = false, defaultValue = "") String cursor,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        final boolean isMember = schedulesDao.ctx()
                .fetchExists(
                        schedulesDao.ctx()
                                .selectOne()
                                .from(MEMBERS)
                                .where(MEMBERS.CLUB_ID.eq(clubId))
                                .and(MEMBERS.USER_ID.eq(signInSession.userId()))
                                .and(MEMBERS.DELETED_AT.isNull())
                );

        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a member of this club");
        }

        var commentsQuery = schedulesDao.ctx()
                .select(
                        SC_ID,
                        SC_SCHEDULE_ID,
                        SC_USER_ID,
                        USER_ADDITIONAL_INFO.NAME.as("user_name"),
                        MEMBERS.PROFILE_IMAGE,
                        SC_CONTENT,
                        SC_CREATED_AT,
                        SC_UPDATED_AT
                )
                .from(SCHEDULE_COMMENTS)
                .leftJoin(USER_ADDITIONAL_INFO).on(USER_ADDITIONAL_INFO.USER_ID.eq(SC_USER_ID))
                .leftJoin(MEMBERS).on(MEMBERS.USER_ID.eq(SC_USER_ID)
                        .and(MEMBERS.CLUB_ID.eq(clubId))
                        .and(MEMBERS.DELETED_AT.isNull()))
                .where(SC_SCHEDULE_ID.eq(scheduleId))
                .and(SC_DELETED_AT.isNull());

        if (!cursor.isEmpty()) {
            commentsQuery = commentsQuery.and(SC_ID.lt(cursor));
        }

        final List<ScheduleCommentItemResponse> comments = commentsQuery
                .orderBy(SC_CREATED_AT.desc())
                .limit(limit)
                .fetch()
                .map(record -> new ScheduleCommentItemResponse(
                        record.get(SC_ID),
                        record.get(SC_SCHEDULE_ID),
                        record.get(SC_USER_ID),
                        record.get("user_name", String.class),
                        record.get(MEMBERS.PROFILE_IMAGE),
                        record.get(SC_CONTENT),
                        record.get(SC_CREATED_AT),
                        record.get(SC_UPDATED_AT)
                ));

        return ResponseEntity.ok(new ScheduleCommentsResponse(comments));
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
}
