package com.official.lockr.domain.club.schedule.application;

import com.official.lockr.domain.club.schedule.application.command.AddScheduleCommentCommand;
import com.official.lockr.domain.club.schedule.application.usecase.AddScheduleCommentUseCase;
import com.official.lockr.domain.club.schedule.domain.ScheduleClub;
import com.official.lockr.domain.club.schedule.domain.ScheduleCommentRepository;
import com.official.lockr.domain.club.schedule.domain.ScheduleRepository;
import org.springframework.stereotype.Service;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class ScheduleCommentService implements AddScheduleCommentUseCase {

    private final ScheduleCommentRepository scheduleCommentRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleClub scheduleClub;

    public ScheduleCommentService(
            final ScheduleCommentRepository scheduleCommentRepository,
            final ScheduleRepository scheduleRepository,
            final ScheduleClub scheduleClub
    ) {
        this.scheduleCommentRepository = scheduleCommentRepository;
        this.scheduleRepository = scheduleRepository;
        this.scheduleClub = scheduleClub;
    }

    @Override
    public void addComment(final AddScheduleCommentCommand command) {
        if (!scheduleClub.isMember(command.userId(), command.clubId())) {
            throw new IllegalArgumentException("클럽 멤버가 아닙니다");
        }

        final var schedule = scheduleRepository.findById(command.scheduleId());
        if (isNull(schedule)) {
            throw new IllegalArgumentException("일정을 찾을 수 없습니다: " + command.scheduleId());
        }

        if (command.content() == null || command.content().isBlank()) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다");
        }

        scheduleCommentRepository.save(generateUlid(), command.scheduleId(), command.clubId(), command.userId(), command.content());
    }
}
