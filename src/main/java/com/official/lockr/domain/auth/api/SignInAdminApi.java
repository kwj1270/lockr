package com.official.lockr.domain.auth.api;

import com.official.lockr.domain.auth.api.dto.AdminLoginHttpRequest;
import com.official.lockr.domain.auth.application.auth.ProcessSignInUseCase;
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

@RequestMapping("/api/v1/admin/auth")
@RestController
public class SignInAdminApi {

    private final HttpHeaders httpHeaders;
    private final ProcessSignInUseCase processSignInUseCase;

    public SignInAdminApi(final HttpHeaders httpHeaders,
                          final ProcessSignInUseCase processSignInUseCase
    ) {
        this.httpHeaders = httpHeaders;
        this.processSignInUseCase = processSignInUseCase;
    }


    @PostMapping("/sign_in")
    public ResponseEntity<Void> loginByAdmin(
            @RequestBody final AdminLoginHttpRequest request,
            final HttpSession session
    ) {
        final HttpHeaderContext httpHeaderContext = httpHeaders.get();
        final SignIn signIn = processSignInUseCase.process(
                request.providerId(), request.providerType(),
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
