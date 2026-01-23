package com.official.lockr.domain.home.schedule;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.schedule.api.dto.ScheduleLocationResponse;
import jakarta.servlet.http.HttpSession;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.SchedulesDao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static org.jooq.generated.tables.SchedulesJOOQEntity.SCHEDULES;
import static org.jooq.generated.tables.AttendancesJOOQEntity.ATTENDANCES;
import static org.jooq.generated.tables.ClubsJOOQEntity.CLUBS;

@RequestMapping("/api/v1/home/schedules")
@RestController
public class HomeScheduleApi {

    private final SchedulesDao schedulesDao;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public HomeScheduleApi(final Configuration configuration) {
        this.schedulesDao = new SchedulesDao(configuration);
    }

    @GetMapping
    public ResponseEntity<HomeSchedulesResponse> schedules(
            final HttpSession httpSession,
            @RequestParam(value = "limit", required = false) String limit,
            @RequestParam(value = "days", required = false) String days
    ) {
        final SignInSession signIn = session(httpSession);

        final int limitCount = limit != null ? Integer.parseInt(limit) : 10;
        final int daysCount = days != null ? Integer.parseInt(days) : 7;

        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime startTime = now.minusHours(2); // 시작시간 + 2시간까지 보여주기 위해
        final LocalDateTime endTime = now.plusDays(daysCount);

        // Step 1: Get schedule IDs where user is ATTENDING
        final List<String> scheduleIds = schedulesDao.ctx()
                .select(ATTENDANCES.SCHEDULE_ID)
                .from(ATTENDANCES)
                .where(ATTENDANCES.USER_ID.eq(signIn.userId()))
                .and(ATTENDANCES.STATUS.eq("ATTENDING"))
                .and(ATTENDANCES.DELETED_AT.isNull())
                .fetch()
                .map(record -> record.get(ATTENDANCES.SCHEDULE_ID));

        if (scheduleIds.isEmpty()) {
            return ResponseEntity.ok(new HomeSchedulesResponse(List.of()));
        }

        // Step 2: Fetch schedules within time range
        final List<HomeScheduleResponse> schedules = schedulesDao.ctx()
                .select(
                        SCHEDULES.ID,
                        SCHEDULES.CLUB_ID,
                        CLUBS.NAME.as("club_name"),
                        SCHEDULES.TITLE,
                        SCHEDULES.TYPE,
                        SCHEDULES.SCHEDULE_TIME,
                        SCHEDULES.LOCATION
                )
                .from(SCHEDULES)
                .innerJoin(CLUBS).on(CLUBS.ID.eq(SCHEDULES.CLUB_ID))
                .where(SCHEDULES.ID.in(scheduleIds))
                .and(SCHEDULES.SCHEDULE_TIME.between(startTime, endTime))
                .and(SCHEDULES.DELETED_AT.isNull())
                .orderBy(SCHEDULES.SCHEDULE_TIME.asc())
                .limit(limitCount)
                .fetch()
                .map(record -> {
                    final LocalDateTime scheduleTime = record.get(SCHEDULES.SCHEDULE_TIME);
                    final long daysUntil = ChronoUnit.DAYS.between(now.toLocalDate(), scheduleTime.toLocalDate());

                    final String locationJson = record.get(SCHEDULES.LOCATION);
                    final ScheduleLocationResponse location = ScheduleLocationResponse.from(locationJson);

                    return new HomeScheduleResponse(
                            record.get(SCHEDULES.ID),
                            record.get(SCHEDULES.CLUB_ID),
                            record.get("club_name", String.class),
                            record.get(SCHEDULES.TITLE),
                            record.get(SCHEDULES.TYPE),
                            scheduleTime.format(FORMATTER),
                            location.name(),
                            (int) daysUntil
                    );
                });

        return ResponseEntity.ok(new HomeSchedulesResponse(schedules));
    }

    private static SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (Objects.isNull(signIn)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return signIn;
    }

    record HomeSchedulesResponse(
            List<HomeScheduleResponse> schedules
    ) {

    }

    record HomeScheduleResponse(
            String id,
            String clubId,
            String clubName,
            String title,
            String type,
            String scheduleTime,
            String locationName,
            int daysUntil
    ) {
    }
}

