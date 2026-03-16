package com.official.lockr.domain.auth.signin.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.auth.signin.domain.SignInToken;
import jakarta.servlet.http.HttpSession;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.SignInTokensDao;
import org.jooq.generated.tables.pojos.SignInTokensEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static java.util.Objects.isNull;
import static org.jooq.generated.tables.SignInTokensJOOQEntity.SIGN_IN_TOKENS;

@RequestMapping("/api/v1/auth/sign_in/tokens")
@RestController
public class SignInTokenQueryApi {

    private final SignInTokensDao signInTokensDao;

    public SignInTokenQueryApi(final Configuration configuration) {
        this.signInTokensDao = new SignInTokensDao(configuration);
    }

    @PostMapping
    public ResponseEntity<SignInToken> signInToken(
            final HttpSession httpSession
    ) {
        final SignInSession signInSession = session(httpSession);
        final String userId = signInSession.userId();

        final SignInTokensEntity tokenEntity = signInTokensDao.ctx()
                .selectFrom(SIGN_IN_TOKENS)
                .where(SIGN_IN_TOKENS.USER_ID.eq(userId))
                .and(SIGN_IN_TOKENS.DELETED_AT.isNull())
                .and(SIGN_IN_TOKENS.EXPIRES_AT.gt(LocalDateTime.now()))
                .orderBy(SIGN_IN_TOKENS.CREATED_AT.desc())
                .limit(1)
                .fetchOneInto(SignInTokensEntity.class);

        if (isNull(tokenEntity)) {
            throw new IllegalStateException("User not authenticated");
        }

        final SignInToken token = new SignInToken(
                tokenEntity.getId(),
                tokenEntity.getUserId(),
                tokenEntity.getSignInId(),
                tokenEntity.getToken(),
                tokenEntity.getExpiresAt(),
                tokenEntity.getCreatedAt(),
                tokenEntity.getDeletedAt()
        );

        return ResponseEntity.ok(token);
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(signIn)) {
            throw new IllegalStateException("User not authenticated");
        }
        return signIn;
    }
}
