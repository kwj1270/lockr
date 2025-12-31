package com.official.lockr.domain.users.domain;

import com.official.lockr.global.vo.BirthDate;
import com.official.lockr.global.vo.Gender;

import java.time.LocalDateTime;
import java.util.Objects;

public class UserAdditionalInfo {

    private final String id;
    private final String userId;
    private String name;
    private BirthDate birthDate;
    private String phone;
    private Gender gender;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static UserAdditionalInfo init(final String id, final String userId) {
        final LocalDateTime now = LocalDateTime.now();
        return new UserAdditionalInfo(id, userId, null, null, null, null, now, now, null);
    }

    public UserAdditionalInfo(final String id,
                              final String userId,
                              final String name,
                              final BirthDate birthDate,
                              final String phone,
                              final Gender gender,
                              final LocalDateTime createdAt,
                              final LocalDateTime updatedAt,
                              final LocalDateTime deletedAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.birthDate = birthDate;
        this.phone = phone;
        this.gender = gender;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public void update(final String name, final BirthDate birthDate, final String phone, final Gender gender) {
        this.name = name;
        this.birthDate = birthDate;
        this.phone = phone;
        this.gender = gender;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getBirthDate() {
        if(Objects.isNull(birthDate)) {
            return null;
        }
        return birthDate.birthDate();
    }

    public String getPhone() {
        return phone;
    }

    public Gender getGender() {
        return gender;
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

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final UserAdditionalInfo that = (UserAdditionalInfo) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getUserId(), that.getUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUserId());
    }

}
