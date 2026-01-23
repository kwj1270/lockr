package com.official.lockr.domain.users.application;

import com.official.lockr.domain.club.club.domain.ClubRepository;
import com.official.lockr.domain.users.application.command.SaveUsersCommand;
import com.official.lockr.domain.users.application.command.UpdateUserAdditionalInfoCommand;
import com.official.lockr.domain.users.application.command.WithdrawUsersCommand;
import com.official.lockr.domain.users.domain.Users;
import com.official.lockr.domain.users.domain.UsersRepository;
import org.springframework.stereotype.Service;


@Service
public class UsersService implements RegisterUsersUsecase, UpdateUserAdditionalInfoUsecase, WithdrawUsersUsecase {

    private final UsersRepository usersRepository;
    private final ClubRepository clubRepository;

    public UsersService(final UsersRepository usersRepository, final ClubRepository clubRepository) {
        this.usersRepository = usersRepository;
        this.clubRepository = clubRepository;
    }

    @Override
    public Users register(final SaveUsersCommand command) {
        final Users users = Users.init();
        return usersRepository.save(users);
    }

    @Override
    public Users updateAdditionalInfo(final UpdateUserAdditionalInfoCommand command) {
        final Users users = usersRepository.findById(command.userId());
        users.updateAdditionalInfo(command.name(), command.birthDate(), command.phone(), command.gender(), command.profileImage());
        return usersRepository.save(users);
    }

    @Override
    public void withdraw(final WithdrawUsersCommand command) {
        final Users users = usersRepository.findById(command.userId());
        if (users == null) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }
        if (!clubRepository.findAllByUserId(command.userId()).isEmpty()) {
            throw new IllegalStateException("클럽에서 먼저 탈퇴해주세요.");
        }
        users.withdraw();
        usersRepository.save(users);
    }
}
