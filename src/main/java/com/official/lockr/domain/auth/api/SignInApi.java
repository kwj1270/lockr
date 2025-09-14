package com.official.lockr.domain.auth.api;

import com.official.lockr.domain.auth.api.dto.AdminLoginHttpRequest;
import com.official.lockr.domain.auth.api.dto.OidcLoginHttpRequest;
import com.official.lockr.domain.auth.application.auth.ProcessSignInUseCase;
import com.official.lockr.domain.auth.application.oidc.RetrieveOidcProviderIdUseCase;
import com.official.lockr.domain.auth.domain.auth.SignIn;
import com.official.lockr.global.mvc.HttpHeaderContext;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1")
@RestController
public class SignInApi {

    private static final String BEARER = "BEARER ";
    private static final String BLANK = "";

    private final HttpHeaderContext requestHeaderContext;
    private final RetrieveOidcProviderIdUseCase retrieveOidcProviderIdUseCase;
    private final ProcessSignInUseCase processSignInUseCase;

    public SignInApi(final HttpHeaderContext requestHeaderContext,
                     final RetrieveOidcProviderIdUseCase retrieveOidcProviderIdUseCase,
                     final ProcessSignInUseCase processSignInUseCase
    ) {
        this.requestHeaderContext = requestHeaderContext;
        this.retrieveOidcProviderIdUseCase = retrieveOidcProviderIdUseCase;
        this.processSignInUseCase = processSignInUseCase;
    }

    @PostMapping("/auth/sign_in/oidc")
    public ResponseEntity<String> loginByOidc(
            @RequestBody final OidcLoginHttpRequest request,
            final HttpSession session
    ) {
        final String idToken = requestHeaderContext.getAuthorization().replace(BEARER, BLANK);
        final String providerId = retrieveOidcProviderIdUseCase.retrieve(idToken, request.providerType());
        final SignIn signIn = processSignInUseCase.process(
                providerId, request.providerType(),
                requestHeaderContext.getDeviceId(),
                requestHeaderContext.getDeviceInfo(),
                requestHeaderContext.getIpAddress(),
                requestHeaderContext.getUserAgent()
        );
        syncSession(session, signIn);
        return ResponseEntity.ok().body("null");
    }

    @PostMapping("/auth/sign_in/admin")
    public ResponseEntity<String> loginByAdmin(
            @RequestBody final AdminLoginHttpRequest request,
            final HttpSession session
    ) {
        final SignIn signIn = processSignInUseCase.process(
                request.providerId(), request.providerType(),
                requestHeaderContext.getDeviceId(),
                requestHeaderContext.getDeviceInfo(),
                requestHeaderContext.getIpAddress(),
                requestHeaderContext.getUserAgent()
        );
        syncSession(session, signIn);
        return ResponseEntity.ok().body("null");
    }

    private void syncSession(final HttpSession session, final SignIn signIn) {
        session.setAttribute("signIn", signIn);
    }
}
