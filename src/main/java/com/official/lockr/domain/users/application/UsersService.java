package com.official.lockr.domain.users.application;

import com.github.f4b6a3.ulid.UlidCreator;
import com.official.lockr.domain.users.application.command.SaveUsersCommand;
import com.official.lockr.domain.users.domain.Users;
import com.official.lockr.domain.users.domain.UsersRepository;
import org.springframework.stereotype.Service;


@Service
public class UsersService implements SaveUsersUsecase {

    private final UsersRepository usersRepository;

    public UsersService(final UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public Users save(final SaveUsersCommand command) {
        final String id = UlidCreator.getUlid().toString();
        final Users users = Users.init(id, command.providerId(), command.providerType());
        return usersRepository.save(users);
    }
}
