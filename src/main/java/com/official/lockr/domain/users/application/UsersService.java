package com.official.lockr.domain.users.application;

import com.official.lockr.domain.users.application.command.SaveUsersCommand;
import com.official.lockr.domain.users.domain.Users;
import com.official.lockr.domain.users.domain.UsersRepository;
import org.springframework.stereotype.Service;


@Service
public class UsersService implements RegisterUsersUsecase {

    private final UsersRepository usersRepository;

    public UsersService(final UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public Users register(final SaveUsersCommand command) {
        final Users users = Users.init();
        return usersRepository.save(users);
    }
}
