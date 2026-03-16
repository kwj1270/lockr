package com.official.lockr.global.util;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static java.util.Objects.isNull;

public final class SessionUtils {

    private SessionUtils() {
    }

    public static SignInSession getSignInSession(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute(SignInSession.SESSION_KEY);
        if (isNull(signIn)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return signIn;
    }

    public static void setSignInSession(final HttpSession session, final SignInSession signInSession) {
        session.setAttribute(SignInSession.SESSION_KEY, signInSession);
    }
}
