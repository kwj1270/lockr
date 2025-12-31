package com.official.lockr.domain.auth.signin.api;

import com.official.lockr.domain.auth.oidc.application.RegisterOidcUseCase;
import com.official.lockr.domain.auth.oidc.domain.Oidc;
import com.official.lockr.domain.auth.signin.api.dto.SignInOidcHttpRequest;
import com.official.lockr.domain.auth.signin.application.usecase.RegisterSignInUseCase;
import com.official.lockr.domain.auth.signin.domain.SignIn;
import com.official.lockr.domain.auth.signin.domain.SignInSession;
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
public class SignInOidcApi {

    private final HttpHeaders httpHeaders;
    private final RegisterOidcUseCase registerOidcUseCase;
    private final RegisterSignInUseCase registerSignInUseCase;

    public SignInOidcApi(final HttpHeaders httpHeaders,
                         final RegisterOidcUseCase registerOidcUseCase,
                         final RegisterSignInUseCase registerSignInUseCase
    ) {
        this.httpHeaders = httpHeaders;
        this.registerOidcUseCase = registerOidcUseCase;
        this.registerSignInUseCase = registerSignInUseCase;
    }

    @PostMapping("/sign_in/oidc")
    public ResponseEntity<Oidc> loginByOidc(
            @RequestBody final SignInOidcHttpRequest request,
            final HttpSession session
    ) {
        final HttpHeaderContext httpHeaderContext = httpHeaders.get();
        final String idToken = httpHeaderContext.authorizationPlain();
        final Oidc oidc = registerOidcUseCase.register(idToken, request.providerType());
        final SignIn signIn = registerSignInUseCase.register(
                oidc.getUserId(), httpHeaderContext.deviceId(), httpHeaderContext.deviceName(), httpHeaderContext.deviceOS(),
                httpHeaderContext.ipAddress(), httpHeaderContext.userAgent()
        );
        session(session, signIn);
        return ResponseEntity.ok().body(oidc);
    }

    private void session(final HttpSession session, final SignIn signIn) {
        session.setAttribute("signIn", new SignInSession(
                signIn.getUserId(),
                signIn.getDeviceId(),
                signIn.getDeviceName(),
                signIn.getDeviceOS(),
                signIn.getIpAddress(),
                signIn.getUserAgent(),
                signIn.getCreatedAt()
        ));
    }
}
