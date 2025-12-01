package com.official.lockr.domain.auth.admin.domain;

import jakarta.annotation.Nullable;

public interface AdminRepository {
    Admin save(Admin admin);

    @Nullable
    Admin findById(String id);

    void deleteById(String id);
}
