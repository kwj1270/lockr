package com.official.lockr.domain.users.application;

import com.official.lockr.domain.users.application.command.SaveUsersCommand;
import com.official.lockr.domain.users.application.command.UpdateUserAdditionalInfoCommand;
import com.official.lockr.domain.users.domain.Users;
import com.official.lockr.domain.users.domain.UsersRepository;
import org.springframework.stereotype.Service;


@Service
public class UsersService implements RegisterUsersUsecase, UpdateUserAdditionalInfoUsecase {

    private final UsersRepository usersRepository;

    public UsersService(final UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public Users register(final SaveUsersCommand command) {
        final Users users = Users.init();
        return usersRepository.save(users);
    }

    @Override
    public Users updateAdditionalInfo(final UpdateUserAdditionalInfoCommand command) {
        final Users users = usersRepository.findById(command.userId());
        users.updateAdditionalInfo(command.name(), command.birthDate(), command.phone(), command.gender());
        return usersRepository.save(users);
    }
}
