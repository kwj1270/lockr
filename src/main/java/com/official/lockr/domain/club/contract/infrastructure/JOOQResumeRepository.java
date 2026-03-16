package com.official.lockr.domain.club.contract.infrastructure;

import com.official.lockr.global.vo.Foot;
import com.official.lockr.global.vo.Position;
import com.official.lockr.domain.club.contract.domain.Resume;
import com.official.lockr.domain.club.contract.domain.ResumeRepository;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.ResumesDao;
import org.jooq.generated.tables.pojos.ResumesEntity;
import org.jooq.generated.tables.records.ResumesRecord;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.jooq.generated.tables.ResumesJOOQEntity.RESUMES;


@Repository
public class JOOQResumeRepository implements ResumeRepository {

    private final ResumesDao resumesDao;

    public JOOQResumeRepository(final Configuration configuration) {
        this.resumesDao = new ResumesDao(configuration);
    }

    @Nullable
    @Override
    public Resume find(final String id) {
        return resumesDao.findOptionalById(id)
                .map(JOOQResumeRepository::domain)
                .orElse(null);
    }

    @Nullable
    @Override
    public Resume findByUserId(final String clubId, final String userId) {
        return resumesDao.ctx()
                .selectFrom(RESUMES)
                .where(
                        RESUMES.CLUB_ID.eq(clubId),
                        RESUMES.USER_ID.eq(userId)
                )
                .fetchOptional()
                .map(JOOQResumeRepository::domain)
                .orElse(null);
    }

    @Override
    public Resume save(final Resume resume) {
        final ResumesEntity resumesEntity = new ResumesEntity(
                resume.getId(),
                resume.getClubId(),
                resume.getUserId(),
                resume.getProfileImage(),
                resume.getBirth(),
                resume.getWeight(),
                resume.getHeight(),
                resume.getName(),
                resume.getEmail(),
                resume.getAddress(),
                resume.getPhone(),
                resume.getEmergencyContactPhone(),
                resume.getNationality(),
                resume.getPreferredPosition().stream().map(Enum::name).collect(Collectors.joining(",")),
                resume.getFoot().name(),
                resume.getAdvantages(),
                resume.getDisadvantages(),
                resume.getCreatedAt(),
                resume.getDeletedAt()
        );
        resumesDao.insert(resumesEntity);
        return domain(resumesEntity);
    }

    private static Resume domain(final ResumesEntity resumesEntity) {
        return new Resume(
                resumesEntity.getId(),
                resumesEntity.getClubId(),
                resumesEntity.getUserId(),
                resumesEntity.getProfileImage(),
                resumesEntity.getBirth(),
                resumesEntity.getWeight(),
                resumesEntity.getHeight(),
                resumesEntity.getName(),
                resumesEntity.getEmail(),
                resumesEntity.getAddress(),
                resumesEntity.getPhone(),
                resumesEntity.getEmergencyContactPhone(),
                resumesEntity.getNationality(),
                Arrays.stream(resumesEntity.getPreferredPosition().split(",")).map(Position::valueOf).toList(),
                Foot.valueOf(resumesEntity.getFoot()),
                resumesEntity.getAdvantages(),
                resumesEntity.getDisadvantages(),
                resumesEntity.getCreatedAt(),
                resumesEntity.getDeletedAt()
        );
    }

    private static Resume domain(final ResumesRecord resumesRecord) {
        return new Resume(
                resumesRecord.getId(),
                resumesRecord.getClubId(),
                resumesRecord.getUserId(),
                resumesRecord.getProfileImage(),
                resumesRecord.getBirth(),
                resumesRecord.getWeight(),
                resumesRecord.getHeight(),
                resumesRecord.getName(),
                resumesRecord.getEmail(),
                resumesRecord.getAddress(),
                resumesRecord.getPhone(),
                resumesRecord.getEmergencyContactPhone(),
                resumesRecord.getNationality(),
                Arrays.stream(resumesRecord.getPreferredPosition().split(",")).map(Position::valueOf).toList(),
                Foot.valueOf(resumesRecord.getFoot()),
                resumesRecord.getAdvantages(),
                resumesRecord.getDisadvantages(),
                resumesRecord.getCreatedAt(),
                resumesRecord.getDeletedAt()
        );
    }
}
