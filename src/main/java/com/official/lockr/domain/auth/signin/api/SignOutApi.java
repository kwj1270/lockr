package com.official.lockr.domain.auth.signin.api;

import com.official.lockr.domain.auth.signin.application.usecase.DeleteSignInTokenUseCase;
import com.official.lockr.domain.auth.signin.domain.SignInSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Objects.isNull;

@RequestMapping("/api/v1/auth/sign_out")
@RestController
public class SignOutApi {

    private final DeleteSignInTokenUseCase deleteSignInTokenUseCase;

    public SignOutApi(final DeleteSignInTokenUseCase deleteSignInTokenUseCase) {
        this.deleteSignInTokenUseCase = deleteSignInTokenUseCase;
    }

    @PostMapping
    public ResponseEntity<Void> logout(
            final HttpSession httpSession
    ) {
        final SignInSession signInSession = (SignInSession) httpSession.getAttribute("signIn");
        if (signInSession != null) {
            final String userId = signInSession.userId();
            deleteSignInTokenUseCase.delete(userId);
        }
        httpSession.invalidate();
        return ResponseEntity.ok().build();
    }
}
