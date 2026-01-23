package com.official.lockr.domain.users.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.users.api.dto.SaveUsersRequest;
import com.official.lockr.domain.users.api.dto.SaveUsersResponse;
import com.official.lockr.domain.users.api.dto.UserAdditionalInfoRequest;
import com.official.lockr.domain.users.application.RegisterUsersUsecase;
import com.official.lockr.domain.users.application.UpdateUserAdditionalInfoUsecase;
import com.official.lockr.domain.users.application.WithdrawUsersUsecase;
import com.official.lockr.domain.users.application.command.SaveUsersCommand;
import com.official.lockr.domain.users.application.command.WithdrawUsersCommand;
import com.official.lockr.domain.users.domain.Users;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import static java.util.Objects.isNull;

@RequestMapping(value = "/api/v1/users")
@RestController
public class UsersApi {

    private final RegisterUsersUsecase registerUsersUsecase;
    private final UpdateUserAdditionalInfoUsecase updateUserAdditionalInfoUsecase;
    private final WithdrawUsersUsecase withdrawUsersUsecase;

    public UsersApi(final RegisterUsersUsecase registerUsersUsecase,
                    final UpdateUserAdditionalInfoUsecase updateUserAdditionalInfoUsecase,
                    final WithdrawUsersUsecase withdrawUsersUsecase) {
        this.registerUsersUsecase = registerUsersUsecase;
        this.updateUserAdditionalInfoUsecase = updateUserAdditionalInfoUsecase;
        this.withdrawUsersUsecase = withdrawUsersUsecase;
    }

    @PostMapping
    public ResponseEntity<SaveUsersResponse> registerUsers(@RequestBody final SaveUsersRequest request) {
        final Users users = registerUsersUsecase.register(new SaveUsersCommand());
        return ResponseEntity.ok(new SaveUsersResponse(users.getId()));
    }

    @PostMapping("/additional-info")
    public ResponseEntity<SaveUsersResponse> registerUserAdditionalInfo(
            final HttpSession httpSession,
            @RequestBody final UserAdditionalInfoRequest request
    ) {
        final SignInSession signInSession = session(httpSession);
        final Users users = updateUserAdditionalInfoUsecase.updateAdditionalInfo(
                request.toCommand(signInSession.userId())
        );
        return ResponseEntity.ok(new SaveUsersResponse(users.getId()));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdrawUser(final HttpSession httpSession) {
        final SignInSession signInSession = session(httpSession);
        withdrawUsersUsecase.withdraw(new WithdrawUsersCommand(signInSession.userId()));
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(signIn)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return signIn;
    }
}
