package com.official.lockr.domain.club.contract.domain;

import com.official.lockr.domain.club.common.Foot;
import com.official.lockr.domain.club.common.Position;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Resume {

    private final String id;
    private final String teamId;
    private final String userId;
    private final String profileImage;
    private final String birth;
    private final String weight;
    private final String height;
    private final String name;
    private final String email;
    private final String address;
    private final String phone;
    private final String emergencyContactPhone;
    private final String nationality;
    private final List<Position> preferredPosition;
    private final Foot foot;
    private final String advantages;
    private final String disadvantages;
    private final LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public Resume(final String id, final String teamId, final String userId, final String profileImage, final String birth, final String weight,
                  final String height, final String name, final String email, final String address, final String phone,
                  final String emergencyContactPhone, final String nationality, final List<Position> preferredPosition,
                  final Foot foot, final String advantages, final String disadvantages, final LocalDateTime createdAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.teamId = teamId;
        this.userId = userId;
        this.profileImage = profileImage;
        this.birth = birth;
        this.weight = weight;
        this.height = height;
        this.name = name;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.emergencyContactPhone = emergencyContactPhone;
        this.nationality = nationality;
        this.preferredPosition = preferredPosition;
        this.foot = foot;
        this.advantages = advantages;
        this.disadvantages = disadvantages;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public String getId() {
        return id;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getUserId() {
        return userId;
    }

    public String getBirth() {
        return birth;
    }

    public String getWeight() {
        return weight;
    }

    public String getHeight() {
        return height;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public String getNationality() {
        return nationality;
    }

    public List<Position> getPreferredPosition() {
        return preferredPosition;
    }

    public Foot getFoot() {
        return foot;
    }

    public String getAdvantages() {
        return advantages;
    }

    public String getDisadvantages() {
        return disadvantages;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public static Resume create(final String id, final String teamId, final String userId, final String profileImage, final String birth, final String weight,
                                final String height, final String name, final String email, final String address, final String phone,
                                final String emergencyContactPhone, final String nationality, final List<Position> preferredPosition,
                                final Foot foot, final String advantages, final String disadvantages) {
        return new Resume(id, teamId, userId, profileImage, birth, weight, height, name, email, address, phone, emergencyContactPhone, nationality, preferredPosition, foot, advantages, disadvantages, LocalDateTime.now(), null);
    }

    public boolean isActive() {
        return Objects.isNull(deletedAt);
    }

    public void cancel() {
        this.deletedAt = LocalDateTime.now();
    }
}
