package com.official.lockr.domain.club.schedule.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.schedule.api.dto.MyScheduleItemResponse;
import com.official.lockr.domain.club.schedule.api.dto.MySchedulesSummaryResponse;
import com.official.lockr.domain.club.schedule.api.dto.ScheduleLocationResponse;
import com.official.lockr.domain.club.schedule.api.dto.ScheduleSummary;
import jakarta.servlet.http.HttpSession;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.SchedulesDao;
import org.jooq.impl.DSL;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.jooq.generated.tables.AttendancesJOOQEntity.ATTENDANCES;
import static org.jooq.generated.tables.ClubsJOOQEntity.CLUBS;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;
import static org.jooq.generated.tables.SchedulesJOOQEntity.SCHEDULES;

@RequestMapping("/api/v1/schedules")
@RestController
public class ScheduleQueryApi {

    private final SchedulesDao schedulesDao;

    public ScheduleQueryApi(final Configuration configuration) {
        this.schedulesDao = new SchedulesDao(configuration);
    }

    @GetMapping("/summary")
    public ResponseEntity<MySchedulesSummaryResponse> mySchedules(
            final HttpSession httpSession,
            @RequestParam(value = "year", required = false) String year,
            @RequestParam(value = "month", required = false) String month
    ) {
        final SignInSession signIn = session(httpSession);
        final LocalDateTime now = LocalDateTime.now();
        final int scheduleYear = parseYear(year, now);
        final int scheduleMonth = parseMonth(month, now);

        final YearMonth yearMonth = YearMonth.of(scheduleYear, scheduleMonth);
        final LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        final LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // Step 1: Get user's club IDs
        final List<String> userClubIds = schedulesDao.ctx()
                .select(MEMBERS.CLUB_ID)
                .from(MEMBERS)
                .where(MEMBERS.USER_ID.eq(signIn.userId()))
                .and(MEMBERS.DELETED_AT.isNull())
                .fetch()
                .map(record -> record.get(MEMBERS.CLUB_ID));

        if (userClubIds.isEmpty()) {
            return ResponseEntity.ok(new MySchedulesSummaryResponse(
                    scheduleYear,
                    scheduleMonth,
                    new ScheduleSummary(0, Map.of()),
                    List.of()
            ));
        }

        // Step 2: Get schedule IDs for the month
        final List<String> scheduleIds = schedulesDao.ctx()
                .select(SCHEDULES.ID)
                .from(SCHEDULES)
                .where(SCHEDULES.CLUB_ID.in(userClubIds))
                .and(SCHEDULES.SCHEDULE_TIME.between(startOfMonth, endOfMonth))
                .and(SCHEDULES.DELETED_AT.isNull())
                .fetch()
                .map(record -> record.get(SCHEDULES.ID));

        if (scheduleIds.isEmpty()) {
            return ResponseEntity.ok(new MySchedulesSummaryResponse(
                    scheduleYear,
                    scheduleMonth,
                    new ScheduleSummary(0, Map.of()),
                    List.of()
            ));
        }

        // Step 3: Get current user's attendance status per schedule
        final Map<String, String> myAttendanceStatuses = schedulesDao.ctx()
                .select(
                        ATTENDANCES.SCHEDULE_ID,
                        ATTENDANCES.STATUS
                )
                .from(ATTENDANCES)
                .where(ATTENDANCES.SCHEDULE_ID.in(scheduleIds))
                .and(ATTENDANCES.USER_ID.eq(signIn.userId()))
                .and(ATTENDANCES.DELETED_AT.isNull())
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        record -> record.get(ATTENDANCES.SCHEDULE_ID),
                        record -> record.get(ATTENDANCES.STATUS)
                ));

        // Step 4: Get attendance counts per schedule
        final Map<String, Integer> attendingCounts = schedulesDao.ctx()
                .select(
                        ATTENDANCES.SCHEDULE_ID,
                        DSL.count().as("count")
                )
                .from(ATTENDANCES)
                .where(ATTENDANCES.SCHEDULE_ID.in(scheduleIds))
                .and(ATTENDANCES.STATUS.eq("ATTENDING"))
                .and(ATTENDANCES.DELETED_AT.isNull())
                .groupBy(ATTENDANCES.SCHEDULE_ID)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        record -> record.get(ATTENDANCES.SCHEDULE_ID),
                        record -> record.get("count", Integer.class)
                ));

        final Map<String, Integer> notAttendingCounts = schedulesDao.ctx()
                .select(
                        ATTENDANCES.SCHEDULE_ID,
                        DSL.count().as("count")
                )
                .from(ATTENDANCES)
                .where(ATTENDANCES.SCHEDULE_ID.in(scheduleIds))
                .and(ATTENDANCES.STATUS.eq("NOT_ATTENDING"))
                .and(ATTENDANCES.DELETED_AT.isNull())
                .groupBy(ATTENDANCES.SCHEDULE_ID)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        record -> record.get(ATTENDANCES.SCHEDULE_ID),
                        record -> record.get("count", Integer.class)
                ));

        final Map<String, Integer> noResponseCounts = schedulesDao.ctx()
                .select(
                        ATTENDANCES.SCHEDULE_ID,
                        DSL.count().as("count")
                )
                .from(ATTENDANCES)
                .where(ATTENDANCES.SCHEDULE_ID.in(scheduleIds))
                .and(ATTENDANCES.STATUS.eq("NO_RESPONSE"))
                .and(ATTENDANCES.DELETED_AT.isNull())
                .groupBy(ATTENDANCES.SCHEDULE_ID)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        record -> record.get(ATTENDANCES.SCHEDULE_ID),
                        record -> record.get("count", Integer.class)
                ));

        // Step 5: Fetch schedule data with club names
        final List<MyScheduleItemResponse> schedules = schedulesDao.ctx()
                .select(
                        SCHEDULES.ID,
                        SCHEDULES.CLUB_ID,
                        CLUBS.NAME.as("club_name"),
                        CLUBS.SPORT_TYPE.as("sport"),
                        SCHEDULES.TITLE,
                        SCHEDULES.TYPE,
                        SCHEDULES.SCHEDULE_TIME,
                        SCHEDULES.LOCATION,
                        SCHEDULES.MAX_PARTICIPANTS,
                        SCHEDULES.STATUS
                )
                .from(SCHEDULES)
                .innerJoin(CLUBS).on(CLUBS.ID.eq(SCHEDULES.CLUB_ID))
                .where(SCHEDULES.ID.in(scheduleIds))
                .orderBy(SCHEDULES.SCHEDULE_TIME.asc())
                .fetch()
                .map(record -> new MyScheduleItemResponse(
                        record.get(SCHEDULES.ID),
                        record.get(SCHEDULES.CLUB_ID),
                        record.get("club_name", String.class),
                        record.get("sport", String.class),
                        record.get(SCHEDULES.TITLE),
                        record.get(SCHEDULES.TYPE),
                        record.get(SCHEDULES.SCHEDULE_TIME),
                        ScheduleLocationResponse.from(record.get(SCHEDULES.LOCATION)),
                        attendingCounts.getOrDefault(record.get(SCHEDULES.ID), 0),
                        notAttendingCounts.getOrDefault(record.get(SCHEDULES.ID), 0),
                        noResponseCounts.getOrDefault(record.get(SCHEDULES.ID), 0),
                        record.get(SCHEDULES.MAX_PARTICIPANTS),
                        myAttendanceStatuses.getOrDefault(record.get(SCHEDULES.ID), "NO_RESPONSE"),
                        record.get(SCHEDULES.STATUS)
                ));

        // Step 6: Calculate summary
        final int totalSchedules = schedules.size();
        final Map<String, Integer> byType = schedules.stream()
                .collect(Collectors.groupingBy(
                        MyScheduleItemResponse::type,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        final ScheduleSummary summary = new ScheduleSummary(totalSchedules, byType);

        return ResponseEntity.ok(new MySchedulesSummaryResponse(
                scheduleYear,
                scheduleMonth,
                summary,
                schedules
        ));
    }

    private static int parseYear(final String year, final LocalDateTime now) {
        if (isNull(year) || year.isBlank()) {
            return now.getYear();
        }
        return Integer.parseInt(year);
    }

    private static int parseMonth(final String month, final LocalDateTime now) {
        if (isNull(month) || month.isBlank()) {
            return now.getMonthValue();
        }
        return Integer.parseInt(month);
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(signIn)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return signIn;
    }
}
