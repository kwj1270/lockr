package com.official.lockr.domain.users.domain;

import com.official.lockr.global.vo.BirthDate;
import com.official.lockr.global.vo.Gender;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.official.lockr.global.util.UlidUtils.generateUlid;

public class Users {

    private final String id;
    private final UserAdditionalInfo userAdditionalInfo;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static Users init() {
        final String userId = generateUlid();
        return new Users(userId, UserAdditionalInfo.init(generateUlid(), userId), LocalDateTime.now(), LocalDateTime.now(), null);
    }

    public Users(final String id, final UserAdditionalInfo userAdditionalInfo,
                 final LocalDateTime createdAt,
                 final LocalDateTime updatedAt,
                 final LocalDateTime deletedAt
    ) {
        this.id = id;
        this.userAdditionalInfo = userAdditionalInfo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public void updateAdditionalInfo(final String name, final BirthDate birthDate, final String phone, final Gender gender, final String profileImage) {
        this.userAdditionalInfo.update(name, birthDate, phone, gender, profileImage);
    }

    public void withdraw() {
        if (this.deletedAt != null) {
            throw new IllegalStateException("이미 탈퇴한 회원입니다.");
        }
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isWithdrawn() {
        return this.deletedAt != null;
    }

    public String getId() {
        return id;
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

    public UserAdditionalInfo getUserAdditionalInfo() {
        return userAdditionalInfo;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Users users = (Users) o;
        return Objects.equals(id, users.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public String birthDate() {
        return userAdditionalInfo.getBirthDate();
    }

    public String name() {
        return userAdditionalInfo.getName();
    }
}
