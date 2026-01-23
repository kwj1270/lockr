package com.official.lockr.domain.auth.admin.application;

import com.official.lockr.domain.auth.admin.application.command.RegisterAdminCommand;
import com.official.lockr.domain.auth.admin.domain.Admin;
import com.official.lockr.domain.auth.admin.domain.AdminRepository;
import com.official.lockr.domain.auth.signup.domain.SignUp;
import com.official.lockr.domain.auth.signup.domain.SignUpRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class AdminService implements RegisterAdminUsecase {

    private final AdminRepository adminRepository;
    private final SignUpRepository signUpRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(final AdminRepository adminRepository,
                        final SignUpRepository signUpRepository,
                        final PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.signUpRepository = signUpRepository;
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
        final SignUp signUp = signUpRepository.save();
        final String userId = signUp.getUserId();
        if (isNull(userId) || userId.isBlank()) {
            throw new IllegalArgumentException();
        }
        try {
            return adminRepository.save(Admin.init(command.id(), userId, passwordEncoder.encode(command.password())));
        } catch (Exception e) {
            signUpRepository.delete(userId);
            throw new IllegalArgumentException(e);
        }
    }
}
