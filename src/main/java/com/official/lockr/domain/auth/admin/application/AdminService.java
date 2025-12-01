package com.official.lockr.domain.auth.admin.application;

import com.official.lockr.domain.auth.admin.application.command.RegisterAdminCommand;
import com.official.lockr.domain.auth.admin.domain.Admin;
import com.official.lockr.domain.auth.admin.domain.AdminRepository;
import com.official.lockr.domain.auth.signup.domain.SignUp;
import com.official.lockr.domain.auth.signup.domain.SignUpRepository;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class AdminService implements RegisterAdminUsecase {

    private final AdminRepository adminRepository;
    private final SignUpRepository signUpRepository;

    public AdminService(final AdminRepository adminRepository, final SignUpRepository signUpRepository) {
        this.adminRepository = adminRepository;
        this.signUpRepository = signUpRepository;
    }

    @Override
    public Admin register(final RegisterAdminCommand command) {
        final Admin admin = admin(command);
        if (admin.hasNotUserId()) {
            return updateUserId(admin);
        }
        if (admin.getPassword().equals(command.password())) {
            return admin;
        }
        throw new IllegalArgumentException();
    }

    private Admin updateUserId(final Admin admin) {
        final SignUp signUp = signUpRepository.save();
        final String userId = signUp.getUserId();
        if (isNull(userId) || userId.isBlank()) {
            throw new IllegalArgumentException();
        }
        admin.setUserId(userId);
        try {
            return adminRepository.save(admin);
        } catch (Exception e) {
            signUpRepository.delete(userId);
            throw e;
        }
    }

    private Admin admin(final RegisterAdminCommand command) {
        final Admin admin = adminRepository.findById(command.id());
        if (nonNull(admin)) {
            return admin;
        }
        return adminRepository.save(Admin.init(command.id(), command.password()));
    }
}
