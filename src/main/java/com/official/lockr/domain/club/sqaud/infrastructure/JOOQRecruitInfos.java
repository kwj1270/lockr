package com.official.lockr.domain.club.sqaud.infrastructure;

import com.official.lockr.domain.club.common.Foot;
import com.official.lockr.domain.club.common.Position;
import com.official.lockr.domain.club.sqaud.domain.squad.RecruitmentInfo;
import com.official.lockr.domain.club.sqaud.domain.squad.RecruitmentInfos;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.ResumesDao;
import org.jooq.generated.tables.records.ResumesRecord;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Objects;

import static org.jooq.generated.tables.ResumesJOOQEntity.RESUMES;

@Repository
public class JOOQRecruitInfos implements RecruitmentInfos {

    private final ResumesDao resumesDao;

    public JOOQRecruitInfos(final Configuration configuration) {
        this.resumesDao = new ResumesDao(configuration);
    }

    @Nullable
    @Override
    public RecruitmentInfo find(final String clubId, final String userId) {
        return resumesDao.ctx()
                .selectFrom(RESUMES)
                .where(
                        RESUMES.CLUB_ID.eq(clubId),
                        RESUMES.USER_ID.eq(userId),
                        RESUMES.DELETED_AT.isNull()
                )
                .fetchOptional()
                .map(JOOQRecruitInfos::domain)
                .orElse(null);
    }

    private static RecruitmentInfo domain(final ResumesRecord record) {
        return new RecruitmentInfo(
                record.getClubId(),
                record.getUserId(),
                record.getProfileImage(),
                record.getBirth(),
                record.getWeight(),
                record.getHeight(),
                record.getName(),
                record.getNationality(),
                Objects.nonNull(record.getPreferredPosition())
                        ? Arrays.stream(record.getPreferredPosition().split(","))
                        .map(Position::valueOf)
                        .toList()
                        : null,
                Objects.nonNull(record.getFoot()) ? Foot.valueOf(record.getFoot()) : null,
                record.getAdvantages(),
                record.getDisadvantages()
        );
    }
}
