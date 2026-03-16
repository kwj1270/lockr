package com.official.lockr.domain.auth.signin.application;

import com.official.lockr.domain.auth.signin.application.command.RefreshSignInTokenCommand;
import com.official.lockr.domain.auth.signin.application.command.RegisterSignInTokenCommand;
import com.official.lockr.domain.auth.signin.application.usecase.DeleteSignInTokenUseCase;
import com.official.lockr.domain.auth.signin.application.usecase.RefreshSignInTokenUseCase;
import com.official.lockr.domain.auth.signin.application.usecase.RegisterSignInTokenUseCase;
import com.official.lockr.domain.auth.signin.domain.SignInToken;
import com.official.lockr.domain.auth.signin.domain.SignInTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.isNull;

@Service
public class SignInTokenService implements RegisterSignInTokenUseCase, RefreshSignInTokenUseCase, DeleteSignInTokenUseCase {

    private final SignInTokenRepository signInTokenRepository;

    public SignInTokenService(final SignInTokenRepository signInTokenRepository) {
        this.signInTokenRepository = signInTokenRepository;
    }

    @Override
    public SignInToken register(final RegisterSignInTokenCommand command) {
        return signInTokenRepository.save(SignInToken.init(command.userId(), command.signInId()));
    }

    @Override
    public SignInToken refresh(final RefreshSignInTokenCommand command) {
        final SignInToken oldToken = signInTokenRepository.findByToken(command.signInToken());
        if (isNull(oldToken) || !oldToken.isValid()) {
            throw new IllegalArgumentException("로그인 필요");
        }
        oldToken.revoke();
        signInTokenRepository.save(oldToken);
        return signInTokenRepository.save(SignInToken.init(oldToken.getUserId(), oldToken.getSignInId()));
    }

    @Override
    public void delete(final String userId) {
        signInTokenRepository.deleteByUserId(userId);
    }
}
