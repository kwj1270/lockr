package com.official.lockr.domain.auth.signin.api;

import com.official.lockr.domain.auth.signin.api.dto.SignInAutoHttpRequest;
import com.official.lockr.domain.auth.signin.api.dto.SignInTokenResponse;
import com.official.lockr.domain.auth.signin.application.usecase.RefreshSignInTokenUseCase;
import com.official.lockr.domain.auth.signin.application.command.RefreshSignInTokenCommand;
import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.auth.signin.domain.SignInToken;
import com.official.lockr.global.http.HttpHeaderContext;
import com.official.lockr.global.http.HttpHeaders;
import com.official.lockr.global.util.SessionUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/auth/sign_in")
@RestController
public class SignInAutoApi {

    private final HttpHeaders httpHeaders;
    private final RefreshSignInTokenUseCase refreshSignInTokenUseCase;

    public SignInAutoApi(final HttpHeaders httpHeaders,
                         final RefreshSignInTokenUseCase refreshSignInTokenUseCase) {
        this.httpHeaders = httpHeaders;
        this.refreshSignInTokenUseCase = refreshSignInTokenUseCase;
    }

    @PostMapping("/auto")
    public ResponseEntity<SignInTokenResponse> signIn(
            final HttpSession session,
            @RequestBody final SignInAutoHttpRequest signInAutoHttpRequest
    ) {
        final HttpHeaderContext httpHeaderContext = httpHeaders.get();
        final SignInToken signInToken = refreshSignInTokenUseCase.refresh(
                new RefreshSignInTokenCommand(signInAutoHttpRequest.signInToken())
        );
        SessionUtils.setSignInSession(session, SignInSession.from(signInToken, httpHeaderContext));
        return ResponseEntity.ok(new SignInTokenResponse(signInToken.getToken(), signInToken.getExpiresAt()));
    }
}
