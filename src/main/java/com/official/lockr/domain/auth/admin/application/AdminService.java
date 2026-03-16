package com.official.lockr.domain.auth.admin.application;

import com.official.lockr.domain.auth.admin.application.command.RegisterAdminCommand;
import com.official.lockr.domain.auth.admin.domain.Admin;
import com.official.lockr.domain.auth.admin.domain.AdminRepository;
import com.official.lockr.domain.users.application.RegisterUsersUseCase;
import com.official.lockr.domain.users.application.command.SaveUsersCommand;
import com.official.lockr.domain.users.domain.Users;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static java.util.Objects.nonNull;

@Service
public class AdminService implements RegisterAdminUseCase {

    private final AdminRepository adminRepository;
    private final RegisterUsersUseCase registerUsersUseCase;
    private final PasswordEncoder passwordEncoder;

    public AdminService(final AdminRepository adminRepository,
                        final RegisterUsersUseCase registerUsersUseCase,
                        final PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.registerUsersUseCase = registerUsersUseCase;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Admin register(final RegisterAdminCommand command) {
        final Admin admin = findOrCreateAdmin(command);
        if (passwordEncoder.matches(command.password(), admin.getPassword())) {
            return admin;
        }
        throw new IllegalArgumentException();
    }

    private Admin findOrCreateAdmin(final RegisterAdminCommand command) {
        final Admin existingAdmin = adminRepository.findById(command.id());
        if (nonNull(existingAdmin)) {
            return existingAdmin;
        }
        return createAdmin(command);
    }

    private Admin createAdmin(final RegisterAdminCommand command) {
        final Users users = registerUsersUseCase.register(new SaveUsersCommand());
        return adminRepository.save(Admin.init(command.id(), users.getId(), passwordEncoder.encode(command.password())));
    }
}
