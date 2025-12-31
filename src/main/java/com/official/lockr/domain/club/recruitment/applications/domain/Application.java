package com.official.lockr.domain.club.recruitment.applications.domain;

import com.official.lockr.domain.club.recruitment.applications.domain.event.ApprovedApplicationEvent;
import com.official.lockr.domain.club.recruitment.applications.domain.event.RejectedApplicationEvent;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.ApplicationStatus;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.ProcessingInfo;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.form.ApplicationFormData;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.form.ApplicationFormType;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.sport.SportSpecificData;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.sport.SportType;
import com.official.lockr.global.ddd.AggregateRoot;
import com.official.lockr.global.vo.BirthDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.util.Objects.isNull;

public class Application extends AggregateRoot {

    private final String id;
    private final String clubId;
    private final String recruitmentId;
    private final String userId;
    private final ApplicationFormType applicationFormType;
    private final ApplicationFormData applicationFormData;
    private final SportType sportType;
    private final SportSpecificData sportSpecificData;
    private ApplicationStatus applicationStatus;
    private ProcessingInfo processingInfo;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static Application create(
            final String id, final String clubId, final String recruitmentId, final String userId,
            final ApplicationFormType applicationFormType,
            final String name, final String phone, final String gender, final String introduction,
            final String profileImageUrl, final String email, final String address, final String birthDate, final String emergencyContactPhone,
            final SportType sportType, final SportSpecificData sportSpecificData
    ) {
        final LocalDateTime now = LocalDateTime.now();
        return new Application(id, clubId, recruitmentId, userId, applicationFormType,
                new ApplicationFormData(name, phone, gender, introduction, new ApplicationFormData.DetailedInfo(profileImageUrl, email, address, new BirthDate(birthDate), emergencyContactPhone)),
                sportType, sportSpecificData, ApplicationStatus.SUBMITTED, null, now, now, null
        );
    }

    public Application(final String id, final String clubId, final String recruitmentId, final String userId, final ApplicationFormType applicationFormType, final ApplicationFormData applicationFormData, final SportType sportType, final SportSpecificData sportSpecificData, final ApplicationStatus applicationStatus, final ProcessingInfo processingInfo, final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.clubId = clubId;
        this.recruitmentId = recruitmentId;
        this.userId = userId;
        this.applicationFormType = applicationFormType;
        this.applicationFormData = applicationFormData;
        this.sportType = sportType;
        this.sportSpecificData = sportSpecificData;
        this.applicationStatus = applicationStatus;
        this.processingInfo = processingInfo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public boolean isActive() {
        return isNull(deletedAt);
    }

    public void cancel() {
        this.applicationStatus = ApplicationStatus.CANCELED;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void approve(final String processedByUserId) {
        this.applicationStatus = ApplicationStatus.APPROVED;
        this.processingInfo = new ProcessingInfo(processedByUserId, LocalDateTime.now(), "");
        this.updatedAt = LocalDateTime.now();
        this.addEvent(new ApprovedApplicationEvent(
                id, clubId, recruitmentId, userId, sportType.name(), applicationStatus.name(), processedByUserId
        ));
    }

    public void reject(final String processedByUserId, final String reason) {
        this.applicationStatus = ApplicationStatus.REJECTED;
        this.processingInfo = new ProcessingInfo(processedByUserId, LocalDateTime.now(), reason);
        this.updatedAt = LocalDateTime.now();
        this.addEvent(new RejectedApplicationEvent(
                id, clubId, recruitmentId, userId, applicationStatus.name(), processedByUserId, reason
        ));
    }

    public boolean isApproved() {
        return ApplicationStatus.APPROVED == applicationStatus;
    }

    public boolean isRejected() {
        return ApplicationStatus.REJECTED == applicationStatus;
    }

    public boolean isApplicant(final String userId) {
        return this.userId.equals(userId);
    }

    public boolean isSameClub(final String clubId) {
        return this.clubId.equals(clubId);
    }

    public String getId() {
        return id;
    }

    public String getClubId() {
        return clubId;
    }

    public String getRecruitmentId() {
        return recruitmentId;
    }

    public String getUserId() {
        return userId;
    }

    public ApplicationFormType getApplicationFormType() {
        return applicationFormType;
    }

    public ApplicationFormData getApplicationFormData() {
        return applicationFormData;
    }

    public SportType getSportType() {
        return sportType;
    }

    public SportSpecificData getSportSpecificData() {
        return sportSpecificData;
    }

    public ApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    public ProcessingInfo getProcessingInfo() {
        return processingInfo;
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

    public String getProfileImage() {
        return applicationFormData.profileImageUrl();
    }

    public BirthDate getBirthDate() {
        return applicationFormData.birthDate();
    }

    public String getName() {
        return applicationFormData.name();
    }

}
