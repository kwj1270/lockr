package com.official.lockr.domain.auth.signin.application;

import com.official.lockr.domain.auth.signin.application.command.RefreshSignInTokenCommand;
import com.official.lockr.domain.auth.signin.application.command.RegisterSignInTokenCommand;
import com.official.lockr.domain.auth.signin.application.usecase.RefreshSignInTokenUseCase;
import com.official.lockr.domain.auth.signin.application.usecase.RegisterSignInTokenUseCase;
import com.official.lockr.domain.auth.signin.domain.SignInToken;
import com.official.lockr.domain.auth.signin.domain.SignInTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static com.official.lockr.global.util.UuidUtils.generateUuid;
import static java.util.Objects.isNull;

@Service
public class SignInTokenService implements RegisterSignInTokenUseCase, RefreshSignInTokenUseCase {

    private final SignInTokenRepository signInTokenRepository;

    public SignInTokenService(final SignInTokenRepository signInTokenRepository) {
        this.signInTokenRepository = signInTokenRepository;
    }

    @Override
    public SignInToken register(final RegisterSignInTokenCommand command) {
        final LocalDateTime now = LocalDateTime.now();
        final SignInToken signInToken = new SignInToken(generateUlid(), command.userId(), command.signInId(), generateUuid(), now.plusDays(30), now, null);
        return signInTokenRepository.save(signInToken);
    }

    @Override
    public SignInToken refresh(final RefreshSignInTokenCommand command) {
        final SignInToken oldToken = signInTokenRepository.findByToken(command.signInToken());
        if (isNull(oldToken) || !oldToken.isValid()) {
            throw new IllegalArgumentException("로그인 필요");
        }
        oldToken.revoke();
        signInTokenRepository.save(oldToken);
        final LocalDateTime now = LocalDateTime.now();
        final SignInToken newToken = new SignInToken(
                generateUlid(),
                oldToken.getUserId(),
                oldToken.getSignInId(),
                generateUuid(),
                now.plusDays(30),
                now,
                null
        );
        return signInTokenRepository.save(newToken);
    }
}
