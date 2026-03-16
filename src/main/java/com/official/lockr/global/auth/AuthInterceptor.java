package com.official.lockr.global.auth;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import static java.util.Objects.isNull;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        final HttpSession httpSession = request.getSession(false);
        if (isNull(httpSession)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        final SignInSession signInSession = (SignInSession) httpSession.getAttribute(SignInSession.SESSION_KEY);
        if (isNull(signInSession)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        request.setAttribute("signInSession", signInSession);
        return true;
    }
}
