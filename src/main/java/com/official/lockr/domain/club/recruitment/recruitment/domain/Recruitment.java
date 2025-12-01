package com.official.lockr.domain.club.recruitment.recruitment.domain;

import com.official.lockr.domain.club.recruitment.recruitment.domain.vo.RecruitmentStatus;
import com.official.lockr.domain.club.recruitment.recruitment.domain.vo.RecruitmentType;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.official.lockr.domain.club.recruitment.recruitment.domain.vo.RecruitmentStatus.RECRUITING;
import static java.util.Objects.nonNull;

public class Recruitment extends AggregateRoot {

    private final String id;
    private final String clubId;
    private String title;
    private String content;
    private boolean isPublic;
    private RecruitmentStatus status;
    private RecruitmentType recruitmentType;
    private String region;
    private List<String> activityDays;
    private String activityTime;
    private String contactMethod;
    private int monthlyFee;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Recruitment(
            final String id,
            final String clubId,
            final RecruitmentStatus status,
            final boolean isPublic,
            final String title,
            final String content,
            final RecruitmentType recruitmentType,
            final String region,
            final List<String> activityDays,
            final String activityTime,
            final int monthlyFee,
            final String contactMethod,
            final LocalDateTime createdAt,
            final LocalDateTime updatedAt,
            final LocalDateTime deletedAt
    ) {
        this.id = id;
        this.clubId = clubId;
        this.status = status;
        this.isPublic = isPublic;
        this.title = title;
        this.content = content;
        this.recruitmentType = recruitmentType;
        this.region = region;
        this.activityDays = activityDays;
        this.activityTime = activityTime;
        this.monthlyFee = monthlyFee;
        this.contactMethod = contactMethod;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static Recruitment post(
            final String id,
            final String clubId,
            final boolean isPublic,
            final String title,
            final String content,
            final RecruitmentType applicationType,
            final String activityRegion,
            final List<String> activityDays,
            final String activityTime,
            final int monthlyFee,
            final String contactMethod
    ) {
        return new Recruitment(
                id, clubId, RECRUITING, isPublic, title, content,
                applicationType, activityRegion, activityDays, activityTime, monthlyFee, contactMethod,
                LocalDateTime.now(), LocalDateTime.now(), null
        );
    }

    public void update(
            final boolean isPublic, final String title, final String content, final String status,
            final RecruitmentType applicationType, final String activityRegion,
            final List<String> activityDays,
            final String activityTime,
            final int monthlyFee,
            final String contactMethod
    ) {
        this.isPublic = isPublic;
        this.title = title;
        this.content = content;
        this.status = RecruitmentStatus.valueOf(status);
        this.recruitmentType = applicationType;
        this.region = activityRegion;
        this.activityDays = activityDays;
        this.activityTime = activityTime;
        this.monthlyFee = monthlyFee;
        this.contactMethod = contactMethod;
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getClubId() {
        return clubId;
    }

    public RecruitmentStatus getStatus() {
        return status;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getRegion() {
        return region;
    }

    public List<String> getActivityDays() {
        return activityDays;
    }

    public String getActivityTime() {
        return activityTime;
    }

    public int getMonthlyFee() {
        return monthlyFee;
    }

    public String getContactMethod() {
        return contactMethod;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public RecruitmentType getRecruitmentType() {
        return recruitmentType;
    }

    public boolean isNotDeleted() {
        return nonNull(deletedAt);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Recruitment that = (Recruitment) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
