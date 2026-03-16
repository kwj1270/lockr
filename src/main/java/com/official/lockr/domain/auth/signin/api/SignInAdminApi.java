package com.official.lockr.domain.auth.signin.api;

import com.official.lockr.domain.auth.admin.application.RegisterAdminUsecase;
import com.official.lockr.domain.auth.admin.application.command.RegisterAdminCommand;
import com.official.lockr.domain.auth.admin.domain.Admin;
import com.official.lockr.domain.auth.signin.api.dto.SignInAdminHttpRequest;
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

@RequestMapping("/api/v1/auth/sign_in")
@RestController
public class SignInAdminApi {

    private final HttpHeaders httpHeaders;
    private final RegisterAdminUsecase registerAdminUsecase;
    private final RegisterSignInUseCase registerSignInUseCase;

    public SignInAdminApi(final HttpHeaders httpHeaders,
                          final RegisterAdminUsecase registerAdminUsecase,
                          final RegisterSignInUseCase registerSignInUseCase
    ) {
        this.httpHeaders = httpHeaders;
        this.registerAdminUsecase = registerAdminUsecase;
        this.registerSignInUseCase = registerSignInUseCase;
    }

    @PostMapping("/admin")
    public ResponseEntity<Admin> loginByAdmin(
            @RequestBody final SignInAdminHttpRequest request,
            final HttpSession session
    ) {
        final HttpHeaderContext httpHeaderContext = httpHeaders.get();
        final Admin admin = registerAdminUsecase.register(new RegisterAdminCommand(request.id(), request.password()));
        final SignIn signIn = registerSignInUseCase.register(
                admin.getUserId(), httpHeaderContext.deviceId(), httpHeaderContext.deviceName(), httpHeaderContext.deviceOS(),
                httpHeaderContext.ipAddress(), httpHeaderContext.userAgent()
        );

        session(session, signIn);
        return ResponseEntity.ok().body(admin);
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
