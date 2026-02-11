package com.official.lockr.domain.auth.admin.api;

import com.official.lockr.domain.auth.admin.api.dto.SignInAdminHttpRequest;
import com.official.lockr.domain.auth.admin.application.RegisterAdminUseCase;
import com.official.lockr.domain.auth.admin.application.command.RegisterAdminCommand;
import com.official.lockr.domain.auth.admin.domain.Admin;
import com.official.lockr.domain.auth.signin.application.command.RegisterSignInCommand;
import com.official.lockr.domain.auth.signin.application.usecase.RegisterSignInUseCase;
import com.official.lockr.domain.auth.signin.domain.SignIn;
import com.official.lockr.domain.auth.signin.domain.SignInSession;
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
public class SignInAdminApi {

    private final HttpHeaders httpHeaders;
    private final RegisterAdminUseCase registerAdminUseCase;
    private final RegisterSignInUseCase registerSignInUseCase;

    public SignInAdminApi(final HttpHeaders httpHeaders,
                          final RegisterAdminUseCase registerAdminUseCase,
                          final RegisterSignInUseCase registerSignInUseCase
    ) {
        this.httpHeaders = httpHeaders;
        this.registerAdminUseCase = registerAdminUseCase;
        this.registerSignInUseCase = registerSignInUseCase;
    }

    @PostMapping("/admin")
    public ResponseEntity<Admin> loginByAdmin(
            @RequestBody final SignInAdminHttpRequest request,
            final HttpSession session
    ) {
        final HttpHeaderContext httpHeaderContext = httpHeaders.get();
        final Admin admin = registerAdminUseCase.register(new RegisterAdminCommand(request.id(), request.password()));
        final SignIn signIn = registerSignInUseCase.register(new RegisterSignInCommand(
                admin.getUserId(), httpHeaderContext.deviceId(), httpHeaderContext.deviceName(), httpHeaderContext.deviceOS(),
                httpHeaderContext.ipAddress(), httpHeaderContext.userAgent()
        ));

        SessionUtils.setSignInSession(session, SignInSession.from(signIn));
        return ResponseEntity.ok().body(admin);
    }
}
