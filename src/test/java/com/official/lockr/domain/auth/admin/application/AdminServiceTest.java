package com.official.lockr.domain.auth.admin.application;

import com.official.lockr.domain.auth.admin.application.command.RegisterAdminCommand;
import com.official.lockr.domain.auth.admin.domain.Admin;
import com.official.lockr.domain.auth.admin.domain.AdminRepository;
import com.official.lockr.domain.users.application.RegisterUsersUseCase;
import com.official.lockr.domain.users.application.command.SaveUsersCommand;
import com.official.lockr.domain.users.domain.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private RegisterUsersUseCase registerUsersUseCase;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(adminRepository, registerUsersUseCase, passwordEncoder);
    }

    @Test
    void shouldReturnAdminWhenExistingAdminLoginWithCorrectPassword() {
        // given
        final String adminId = "admin1";
        final String rawPassword = "password123";
        final String encodedPassword = "encodedPassword123";
        final LocalDateTime now = LocalDateTime.now();
        final Admin existingAdmin = new Admin(adminId, encodedPassword, "user123", "BASIC", now, now, null);

        when(adminRepository.findById(adminId)).thenReturn(existingAdmin);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        final RegisterAdminCommand command = new RegisterAdminCommand(adminId, rawPassword);

        // when
        final Admin result = adminService.register(command);

        // then
        assertThat(result).isEqualTo(existingAdmin);
        assertThat(result.getUserId()).isEqualTo("user123");
    }

    @Test
    void shouldThrowExceptionWhenExistingAdminLoginWithWrongPassword() {
        // given
        final String adminId = "admin1";
        final String encodedPassword = "encodedPassword123";
        final String wrongPassword = "wrongPassword";
        final LocalDateTime now = LocalDateTime.now();
        final Admin existingAdmin = new Admin(adminId, encodedPassword, "user123", "BASIC", now, now, null);

        when(adminRepository.findById(adminId)).thenReturn(existingAdmin);
        when(passwordEncoder.matches(wrongPassword, encodedPassword)).thenReturn(false);

        final RegisterAdminCommand command = new RegisterAdminCommand(adminId, wrongPassword);

        // when & then
        assertThatThrownBy(() -> adminService.register(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldCreateNewAdminWhenIdDoesNotExist() {
        // given
        final String adminId = "newAdmin";
        final String rawPassword = "password123";
        final String encodedPassword = "encodedPassword123";
        final String generatedUserId = "generatedUserId123";

        when(adminRepository.findById(adminId)).thenReturn(null);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(registerUsersUseCase.register(any(SaveUsersCommand.class))).thenReturn(Users.init());

        final RegisterAdminCommand command = new RegisterAdminCommand(adminId, rawPassword);

        // when
        final Admin result = adminService.register(command);

        // then
        assertThat(result.getId()).isEqualTo(adminId);
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void shouldAssignUserIdFromRegisterUsersUseCaseWhenCreatingNewAdmin() {
        // given
        final String adminId = "newAdmin";
        final String rawPassword = "password123";
        final String encodedPassword = "encodedPassword123";
        final String generatedUserId = "generatedUserId123";
        final Users users = new Users(generatedUserId, null, LocalDateTime.now(), LocalDateTime.now(), null);

        when(adminRepository.findById(adminId)).thenReturn(null);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(registerUsersUseCase.register(any(SaveUsersCommand.class))).thenReturn(users);

        final RegisterAdminCommand command = new RegisterAdminCommand(adminId, rawPassword);

        // when
        final Admin result = adminService.register(command);

        // then
        assertThat(result.getUserId()).isEqualTo(generatedUserId);
        verify(registerUsersUseCase).register(any(SaveUsersCommand.class));
    }

    @Test
    void shouldEncodePasswordWhenCreatingNewAdmin() {
        // given
        final String adminId = "newAdmin";
        final String rawPassword = "password123";
        final String encodedPassword = "encodedPassword123";
        final String generatedUserId = "generatedUserId123";

        when(adminRepository.findById(adminId)).thenReturn(null);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(registerUsersUseCase.register(any(SaveUsersCommand.class))).thenReturn(Users.init());

        final RegisterAdminCommand command = new RegisterAdminCommand(adminId, rawPassword);

        // when
        final Admin result = adminService.register(command);

        // then
        assertThat(result.getPassword()).isEqualTo(encodedPassword);
        verify(passwordEncoder).encode(rawPassword);
    }
}
