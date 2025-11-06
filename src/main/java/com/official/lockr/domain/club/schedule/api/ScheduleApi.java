package com.official.lockr.domain.club.schedule.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.official.lockr.domain.auth.domain.auth.SignInSession;
import com.official.lockr.domain.club.schedule.api.dto.CreateScheduleRequest;
import com.official.lockr.domain.club.schedule.api.dto.RespondToScheduleRequest;
import com.official.lockr.domain.club.schedule.api.dto.ScheduleResponse;
import com.official.lockr.domain.club.schedule.api.dto.UpdateScheduleRequest;
import com.official.lockr.domain.club.schedule.application.dto.CreateScheduleCommand;
import com.official.lockr.domain.club.schedule.application.dto.RespondToScheduleCommand;
import com.official.lockr.domain.club.schedule.application.dto.UpdateScheduleCommand;
import com.official.lockr.domain.club.schedule.application.usecase.CancelScheduleUseCase;
import com.official.lockr.domain.club.schedule.application.usecase.CreateScheduleUseCase;
import com.official.lockr.domain.club.schedule.application.usecase.RespondToScheduleUseCase;
import com.official.lockr.domain.club.schedule.application.usecase.UpdateScheduleUseCase;
import com.official.lockr.domain.club.schedule.domain.Schedule;
import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.official.lockr.domain.club.schedule.domain.detail.MatchDetail;
import com.official.lockr.domain.club.schedule.domain.detail.ScheduleDetail;
import com.official.lockr.domain.club.schedule.domain.detail.SocialEventDetail;
import com.official.lockr.domain.club.schedule.domain.detail.TrainingDetail;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static java.util.Objects.isNull;

@RequestMapping("/api/v1/clubs/{clubId}/schedules")
@RestController
public class ScheduleApi {

    private final CreateScheduleUseCase createScheduleUseCase;
    private final RespondToScheduleUseCase respondToScheduleUseCase;
    private final UpdateScheduleUseCase updateScheduleUseCase;
    private final CancelScheduleUseCase cancelScheduleUseCase;
    private final ObjectMapper objectMapper;

    public ScheduleApi(
            final CreateScheduleUseCase createScheduleUseCase,
            final RespondToScheduleUseCase respondToScheduleUseCase,
            final UpdateScheduleUseCase updateScheduleUseCase,
            final CancelScheduleUseCase cancelScheduleUseCase,
            final ObjectMapper objectMapper
    ) {
        this.createScheduleUseCase = createScheduleUseCase;
        this.respondToScheduleUseCase = respondToScheduleUseCase;
        this.updateScheduleUseCase = updateScheduleUseCase;
        this.cancelScheduleUseCase = cancelScheduleUseCase;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @RequestBody final CreateScheduleRequest request
    ) {
        final SignInSession session = session(httpSession);

        final Schedule schedule = createScheduleUseCase.create(new CreateScheduleCommand(
                session.userId(),
                clubId,
                request.title(),
                request.content(),
                request.location(),
                request.scheduleTime(),
                request.scheduleType(),
                scheduleDetail(request.scheduleType(), request.detail())
        ));

        return ResponseEntity.ok(ScheduleResponse.from(schedule));
    }

    private ScheduleDetail scheduleDetail(final ScheduleType scheduleType, final String detail) {
        return switch (scheduleType) {
            case MATCH -> scheduleDetail(detail, MatchDetail.class);
            case TRAINING -> scheduleDetail(detail, TrainingDetail.class);
            case SOCIAL_EVENT -> scheduleDetail(detail, SocialEventDetail.class);
        };
    }

    private ScheduleDetail scheduleDetail(final String detail, final Class<? extends ScheduleDetail> classType) {
        try {
            return objectMapper.readValue(detail, classType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException();
        }
    }

    @PostMapping("/{scheduleId}/responses")
    public ResponseEntity<Schedule> respondToSchedule(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String scheduleId,
            @RequestBody final RespondToScheduleRequest request
    ) {
        final SignInSession session = session(httpSession);

        final Schedule schedule = respondToScheduleUseCase.respond(new RespondToScheduleCommand(
                scheduleId,
                session.userId(),
                clubId,
                request.status(),
                request.reason()
        ));

        return ResponseEntity.ok().body(schedule);
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<Schedule> updateSchedule(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String scheduleId,
            @RequestBody final UpdateScheduleRequest request
    ) {
        final SignInSession session = session(httpSession);

        final Schedule schedule = updateScheduleUseCase.update(new UpdateScheduleCommand(
                scheduleId,
                session.userId(),
                clubId,
                request.title(),
                request.content(),
                request.location(),
                request.scheduleTime(),
                request.detail()
        ));

        return ResponseEntity.ok().body(schedule);
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Schedule> cancelSchedule(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String scheduleId
    ) {
        final SignInSession session = session(httpSession);

        final Schedule schedule = cancelScheduleUseCase.cancel(session.userId(), clubId, scheduleId);

        return ResponseEntity.ok().body(schedule);
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession session = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(session)) {
            throw new IllegalArgumentException("Not signed in");
        }
        return session;
    }
}
