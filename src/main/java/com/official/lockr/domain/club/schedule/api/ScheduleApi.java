package com.official.lockr.domain.club.schedule.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.schedule.api.dto.*;
import com.official.lockr.domain.club.schedule.application.usecase.CancelScheduleUseCase;
import com.official.lockr.domain.club.schedule.application.usecase.RegisterScheduleUseCase;
import com.official.lockr.domain.club.schedule.application.usecase.RespondToScheduleUseCase;
import com.official.lockr.domain.club.schedule.application.usecase.UpdateScheduleUseCase;
import com.official.lockr.domain.club.schedule.domain.Schedule;
import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.official.lockr.domain.club.schedule.domain.vo.MatchDetailData;
import com.official.lockr.domain.club.schedule.domain.vo.ScheduleDetailData;
import com.official.lockr.domain.club.schedule.domain.vo.SocialDetailData;
import com.official.lockr.domain.club.schedule.domain.vo.TrainingDetailData;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import static java.util.Objects.isNull;

@RequestMapping("/api/v1/clubs/{clubId}/schedules")
@RestController
public class ScheduleApi {

    private final RegisterScheduleUseCase registerScheduleUseCase;
    private final UpdateScheduleUseCase updateScheduleUseCase;
    private final RespondToScheduleUseCase respondToScheduleUseCase;
    private final CancelScheduleUseCase cancelScheduleUseCase;
    private final ObjectMapper objectMapper;

    public ScheduleApi(
            final RegisterScheduleUseCase registerScheduleUseCase,
            final UpdateScheduleUseCase updateScheduleUseCase,
            final CancelScheduleUseCase cancelScheduleUseCase,
            final RespondToScheduleUseCase respondToScheduleUseCase,
            final ObjectMapper objectMapper
    ) {
        this.registerScheduleUseCase = registerScheduleUseCase;
        this.updateScheduleUseCase = updateScheduleUseCase;
        this.cancelScheduleUseCase = cancelScheduleUseCase;
        this.respondToScheduleUseCase = respondToScheduleUseCase;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> register(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @RequestBody final CreateScheduleRequest request
    ) {
        final SignInSession session = session(httpSession);
        final Schedule schedule = registerScheduleUseCase.create(
                session.userId(), clubId, request.title(), request.content(),
                request.location(), request.scheduleTime(), request.scheduleType(),
                scheduleDetail(request.scheduleType(), request.detail()),
                request.minParticipants(), request.maxParticipants(), request.deadlineDays()
        );
        return ResponseEntity.ok(ScheduleResponse.from(schedule));
    }

    @PostMapping("/{scheduleId}/update")
    public ResponseEntity<Schedule> update(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String scheduleId,
            @RequestBody final UpdateScheduleRequest request
    ) {
        final SignInSession session = session(httpSession);
        final Schedule schedule = updateScheduleUseCase.update(
                scheduleId, session.userId(), clubId, request.title(), request.content(),
                request.location(), request.scheduleTime(), request.detail(),
                request.minParticipants(), request.maxParticipants(), request.deadlineDays()
        );
        return ResponseEntity.ok().body(schedule);
    }

    @PostMapping("/{scheduleId}/cancel")
    public ResponseEntity<Schedule> cancelSchedule(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String scheduleId
    ) {
        final SignInSession session = session(httpSession);
        final Schedule schedule = cancelScheduleUseCase.cancel(session.userId(), clubId, scheduleId);
        return ResponseEntity.ok().body(schedule);
    }

    @PostMapping("/{scheduleId}/respond")
    public ResponseEntity<Void> respond(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String scheduleId,
            @RequestBody final RespondToScheduleRequest request
    ) {
        final SignInSession session = session(httpSession);
        respondToScheduleUseCase.respond(
                scheduleId, session.userId(), clubId, request.status(), request.reason()
        );
        // Command API는 성공 응답만 반환
        return ResponseEntity.ok().build();
    }

    private ScheduleDetailData scheduleDetail(final ScheduleType scheduleType, final String detail) {
        return switch (scheduleType) {
            case MATCH -> scheduleDetail(detail, MatchDetailData.class);
            case TRAINING -> scheduleDetail(detail, TrainingDetailData.class);
            case SOCIAL_EVENT -> scheduleDetail(detail, SocialDetailData.class);
        };
    }

    private ScheduleDetailData scheduleDetail(final String detail, final Class<? extends ScheduleDetailData> classType) {
        try {
            return objectMapper.readValue(detail, classType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException();
        }
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(signIn)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return signIn;
    }
}
