package com.official.lockr.domain.auth.api;

import com.official.lockr.domain.auth.api.dto.OidcLoginHttpRequest;
import com.official.lockr.domain.auth.application.auth.ProcessSignInUseCase;
import com.official.lockr.domain.auth.application.oidc.RetrieveOidcProviderIdUseCase;
import com.official.lockr.domain.auth.domain.signin.SignIn;
import com.official.lockr.domain.auth.domain.signin.SignInSession;
import com.official.lockr.global.http.HttpHeaderContext;
import com.official.lockr.global.http.HttpHeaders;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/auth")
@RestController
public class SignInApi {

    private final HttpHeaders httpHeaders;
    private final RetrieveOidcProviderIdUseCase retrieveOidcProviderIdUseCase;
    private final ProcessSignInUseCase processSignInUseCase;

    public SignInApi(final HttpHeaders httpHeaders,
                     final RetrieveOidcProviderIdUseCase retrieveOidcProviderIdUseCase,
                     final ProcessSignInUseCase processSignInUseCase
    ) {
        this.httpHeaders = httpHeaders;
        this.retrieveOidcProviderIdUseCase = retrieveOidcProviderIdUseCase;
        this.processSignInUseCase = processSignInUseCase;
    }

    @PostMapping("/sign_in/oidc")
    public ResponseEntity<Void> loginByOidc(
            @RequestBody final OidcLoginHttpRequest request,
            final HttpSession session
    ) {
        final HttpHeaderContext httpHeaderContext = httpHeaders.get();
        final String idToken = httpHeaderContext.authorizationPlain();
        final String providerId = retrieveOidcProviderIdUseCase.retrieve(idToken, request.providerType());
        final SignIn signIn = processSignInUseCase.process(
                providerId, request.providerType(),
                httpHeaderContext.deviceId(), httpHeaderContext.deviceInfo(),
                httpHeaderContext.ipAddress(), httpHeaderContext.userAgent()
        );
        syncSession(session, signIn);
        return ResponseEntity.ok().build();
    }

    private void syncSession(final HttpSession session, final SignIn signIn) {
        session.setAttribute("signIn", new SignInSession(signIn));
    }
}
