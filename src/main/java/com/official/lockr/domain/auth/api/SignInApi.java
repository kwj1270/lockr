package com.official.lockr.domain.auth.api;

import com.official.lockr.domain.auth.api.dto.AdminLoginHttpRequest;
import com.official.lockr.domain.auth.api.dto.OidcLoginHttpRequest;
import com.official.lockr.domain.auth.application.auth.ProcessSignInUseCase;
import com.official.lockr.domain.auth.application.oidc.RetrieveOidcProviderIdUseCase;
import com.official.lockr.domain.auth.domain.auth.SignIn;
import com.official.lockr.global.http.HttpHeaderContext;
import com.official.lockr.global.http.HttpHeaders;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1")
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

    @PostMapping("/auth/sign_in/oidc")
    public ResponseEntity<String> loginByOidc(
            @RequestBody final OidcLoginHttpRequest request,
            final HttpSession session
    ) {
        final HttpHeaderContext httpHeaderContext = httpHeaders.get();
        final String idToken = httpHeaderContext.authorizationPlain();
        final String providerId = retrieveOidcProviderIdUseCase.retrieve(idToken, request.providerType());
        final SignIn signIn = processSignInUseCase.process(
                providerId,
                request.providerType(),
                httpHeaderContext.deviceId(),
                httpHeaderContext.deviceInfo(),
                httpHeaderContext.ipAddress(),
                httpHeaderContext.userAgent()
        );
        syncSession(session, signIn);
        return ResponseEntity.ok().body("null");
    }

    @PostMapping("/auth/sign_in/admin")
    public ResponseEntity<String> loginByAdmin(
            @RequestBody final AdminLoginHttpRequest request,
            final HttpSession session
    ) {
        final HttpHeaderContext httpHeaderContext = httpHeaders.get();
        final SignIn signIn = processSignInUseCase.process(
                request.providerId(),
                request.providerType(),
                httpHeaderContext.deviceId(),
                httpHeaderContext.deviceInfo(),
                httpHeaderContext.ipAddress(),
                httpHeaderContext.userAgent()
        );
        syncSession(session, signIn);
        return ResponseEntity.ok().body("null");
    }

    private void syncSession(final HttpSession session, final SignIn signIn) {
        session.setAttribute("signIn", signIn);
    }
}
