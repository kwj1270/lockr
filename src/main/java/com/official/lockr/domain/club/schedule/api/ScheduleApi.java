package com.official.lockr.domain.club.schedule.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.schedule.api.dto.*;
import com.official.lockr.domain.club.schedule.application.command.CancelScheduleCommand;
import com.official.lockr.domain.club.schedule.application.usecase.AdminUpdateAttendanceUseCase;
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
import com.official.lockr.global.util.SessionUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/clubs/{clubId}/schedules")
@RestController
public class ScheduleApi {

    private final RegisterScheduleUseCase registerScheduleUseCase;
    private final UpdateScheduleUseCase updateScheduleUseCase;
    private final RespondToScheduleUseCase respondToScheduleUseCase;
    private final CancelScheduleUseCase cancelScheduleUseCase;
    private final AdminUpdateAttendanceUseCase adminUpdateAttendanceUseCase;
    private final ObjectMapper objectMapper;

    public ScheduleApi(
            final RegisterScheduleUseCase registerScheduleUseCase,
            final UpdateScheduleUseCase updateScheduleUseCase,
            final CancelScheduleUseCase cancelScheduleUseCase,
            final RespondToScheduleUseCase respondToScheduleUseCase,
            final AdminUpdateAttendanceUseCase adminUpdateAttendanceUseCase,
            final ObjectMapper objectMapper
    ) {
        this.registerScheduleUseCase = registerScheduleUseCase;
        this.updateScheduleUseCase = updateScheduleUseCase;
        this.cancelScheduleUseCase = cancelScheduleUseCase;
        this.respondToScheduleUseCase = respondToScheduleUseCase;
        this.adminUpdateAttendanceUseCase = adminUpdateAttendanceUseCase;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> register(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @Valid @RequestBody final CreateScheduleRequest request
    ) {
        final SignInSession session = SessionUtils.getSignInSession(httpSession);
        final Schedule schedule = registerScheduleUseCase.create(
                request.toCommand(session.userId(), clubId, scheduleDetail(request.scheduleType(), request.detail()))
        );
        return ResponseEntity.ok(ScheduleResponse.from(schedule));
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> update(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String scheduleId,
            @Valid @RequestBody final UpdateScheduleRequest request
    ) {
        final SignInSession session = SessionUtils.getSignInSession(httpSession);
        final Schedule schedule = updateScheduleUseCase.update(
                request.toCommand(scheduleId, session.userId(), clubId)
        );
        return ResponseEntity.ok(ScheduleResponse.from(schedule));
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> cancelSchedule(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String scheduleId
    ) {
        final SignInSession session = SessionUtils.getSignInSession(httpSession);
        final Schedule schedule = cancelScheduleUseCase.cancel(
                new CancelScheduleCommand(session.userId(), clubId, scheduleId)
        );
        return ResponseEntity.ok(ScheduleResponse.from(schedule));
    }

    @PutMapping("/{scheduleId}/respond")
    public ResponseEntity<Void> respond(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String scheduleId,
            @Valid @RequestBody final RespondToScheduleRequest request
    ) {
        final SignInSession session = SessionUtils.getSignInSession(httpSession);
        respondToScheduleUseCase.respond(
                request.toCommand(scheduleId, session.userId(), clubId)
        );
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{scheduleId}/attendances/{targetUserId}")
    public ResponseEntity<Void> adminUpdateAttendance(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String scheduleId,
            @PathVariable final String targetUserId,
            @Valid @RequestBody final AdminUpdateAttendanceRequest request
    ) {
        adminUpdateAttendanceUseCase.update(
                request.toCommand(scheduleId, signInSession.userId(), clubId, targetUserId)
        );
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
            throw new IllegalArgumentException("Invalid schedule detail format: " + e.getMessage());
        }
    }

}
