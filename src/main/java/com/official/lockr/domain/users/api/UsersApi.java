package com.official.lockr.domain.users.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.users.api.dto.SaveUsersRequest;
import com.official.lockr.domain.users.api.dto.SaveUsersResponse;
import com.official.lockr.domain.users.api.dto.UserAdditionalInfoRequest;
import com.official.lockr.domain.users.application.RegisterUsersUseCase;
import com.official.lockr.domain.users.application.UpdateUserAdditionalInfoUseCase;
import com.official.lockr.domain.users.application.WithdrawUsersUseCase;
import com.official.lockr.domain.users.application.command.SaveUsersCommand;
import com.official.lockr.domain.users.application.command.WithdrawUsersCommand;
import com.official.lockr.domain.users.domain.Users;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/v1/users")
@RestController
public class UsersApi {

    private final RegisterUsersUseCase registerUsersUseCase;
    private final UpdateUserAdditionalInfoUseCase updateUserAdditionalInfoUseCase;
    private final WithdrawUsersUseCase withdrawUsersUseCase;

    public UsersApi(final RegisterUsersUseCase registerUsersUseCase,
                    final UpdateUserAdditionalInfoUseCase updateUserAdditionalInfoUseCase,
                    final WithdrawUsersUseCase withdrawUsersUseCase) {
        this.registerUsersUseCase = registerUsersUseCase;
        this.updateUserAdditionalInfoUseCase = updateUserAdditionalInfoUseCase;
        this.withdrawUsersUseCase = withdrawUsersUseCase;
    }

    @PostMapping
    public ResponseEntity<SaveUsersResponse> registerUsers(@RequestBody final SaveUsersRequest request) {
        final Users users = registerUsersUseCase.register(new SaveUsersCommand());
        return ResponseEntity.ok(new SaveUsersResponse(users.getId()));
    }

    @PostMapping("/additional-info")
    public ResponseEntity<SaveUsersResponse> registerUserAdditionalInfo(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @RequestBody final UserAdditionalInfoRequest request
    ) {
        final Users users = updateUserAdditionalInfoUseCase.updateAdditionalInfo(
                request.toCommand(signInSession.userId())
        );
        return ResponseEntity.ok(new SaveUsersResponse(users.getId()));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdrawUser(@RequestAttribute("signInSession") final SignInSession signInSession) {
        withdrawUsersUseCase.withdraw(new WithdrawUsersCommand(signInSession.userId()));
        return ResponseEntity.ok().build();
    }

}
