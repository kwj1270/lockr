package com.official.lockr.domain.users.domain;

public interface UsersRepository {
    Users save(final Users users);

    Users findById(final String userId);
}
