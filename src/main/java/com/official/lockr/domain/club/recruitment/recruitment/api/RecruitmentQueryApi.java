package com.official.lockr.domain.club.recruitment.recruitment.api;

import com.official.lockr.domain.club.recruitment.recruitment.api.dto.RecruitmentResponse;
import com.official.lockr.domain.club.recruitment.recruitment.api.dto.RecruitmentsResponse;
import com.official.lockr.domain.club.recruitment.recruitment.domain.Recruitment;
import com.official.lockr.domain.club.recruitment.recruitment.domain.vo.RecruitmentStatus;
import org.jooq.Configuration;
import org.jooq.generated.tables.RecruitmentsJOOQEntity;
import org.jooq.generated.tables.daos.RecruitmentsDao;
import org.jooq.generated.tables.pojos.RecruitmentsEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;
import static org.jooq.generated.tables.RecruitmentsJOOQEntity.RECRUITMENTS;

@RestController
@RequestMapping("/api/v1/recruitments")
public class RecruitmentQueryApi {

    private final RecruitmentsDao recruitmentsDao;

    public RecruitmentQueryApi(final Configuration configuration) {
        this.recruitmentsDao = new RecruitmentsDao(configuration);
    }

    @GetMapping
    public ResponseEntity<RecruitmentsResponse> getRecruitments(
            @RequestParam(value = "recruitmentId", required = false, defaultValue = "") String recruitmentId,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        var query = recruitmentsDao.ctx()
                .selectFrom(RECRUITMENTS)
                .where(RECRUITMENTS.IS_PUBLIC.eq(true));

        if (!recruitmentId.isEmpty()) {
            query = query.and(RECRUITMENTS.ID.lt(recruitmentId));
        }

        final RecruitmentsResponse responses = new RecruitmentsResponse(query
                .orderBy(RECRUITMENTS.CREATED_AT.desc())
                .limit(limit)
                .fetchInto(RecruitmentsEntity.class)
                .stream()
                .map(entity -> new RecruitmentResponse(
                        entity.getId(),
                        entity.getClubId(),
                        RecruitmentStatus.valueOf(entity.getStatus()),
                        entity.getIsPublic(),
                        entity.getTitle(),
                        entity.getContent(),
                        entity.getActivityCity(),
                        entity.getActivityDistrict(),
                        Arrays.stream(entity.getActivityDays().split(",")).toList(),
                        entity.getActivityTime(),
                        entity.getMonthlyFee(),
                        entity.getContactMethod(),
                        entity.getCreatedAt(),
                        entity.getUpdatedAt()
                ))
                .toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{recruitmentId}")
    public ResponseEntity<RecruitmentResponse> getRecruitmentById(
            @PathVariable final String recruitmentId
    ) {
        final RecruitmentsEntity recruitments = recruitmentsDao.findById(recruitmentId);
        if (isNull(recruitments)) {
            return null;
        }
        return ResponseEntity.ok().body(new RecruitmentResponse(
                recruitments.getId(),
                recruitments.getClubId(),
                RecruitmentStatus.valueOf(recruitments.getStatus()),
                recruitments.getIsPublic(),
                recruitments.getTitle(),
                recruitments.getContent(),
                recruitments.getActivityCity(),
                recruitments.getActivityDistrict(),
                Arrays.stream(recruitments.getActivityDays().split(",")).toList(),
                recruitments.getActivityTime(),
                recruitments.getMonthlyFee(),
                recruitments.getContactMethod(),
                recruitments.getCreatedAt(),
                recruitments.getUpdatedAt()
        ));
    }
}
