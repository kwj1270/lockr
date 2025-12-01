package com.official.lockr.domain.auth.signin.application;

import com.official.lockr.domain.auth.signin.application.command.RefreshSignInTokenCommand;
import com.official.lockr.domain.auth.signin.application.command.RegisterSignInTokenCommand;
import com.official.lockr.domain.auth.signin.domain.SignInToken;
import com.official.lockr.domain.auth.signin.domain.SignInTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignInTokenServiceTest {

    @Mock
    private SignInTokenRepository signInTokenRepository;

    private SignInTokenService signInTokenService;

    @BeforeEach
    void setUp() {
        signInTokenService = new SignInTokenService(signInTokenRepository);
    }

    @Test
    void shouldRegisterNewSignInToken() {
        // given
        final RegisterSignInTokenCommand command = new RegisterSignInTokenCommand("user123", "signIn456");
        final SignInToken savedToken = new SignInToken("token1", "user123", "signIn456", "abc123", LocalDateTime.now().plusDays(30), LocalDateTime.now(), null);

        when(signInTokenRepository.save(any(SignInToken.class))).thenReturn(savedToken);

        // when
        final SignInToken result = signInTokenService.register(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("user123");
        assertThat(result.getSignInId()).isEqualTo("signIn456");
        verify(signInTokenRepository, times(1)).save(any(SignInToken.class));
    }

    @Test
    void shouldRefreshTokenByCreatingNewTokenAndRevokingOld() {
        // given
        final String oldTokenValue = "oldToken123";
        final LocalDateTime now = LocalDateTime.now();
        final SignInToken oldToken = new SignInToken(
                "oldTokenId",
                "user123",
                "signIn456",
                oldTokenValue,
                now.plusDays(1), // 유효한 토큰
                now.minusDays(30),
                null
        );

        final RefreshSignInTokenCommand command = new RefreshSignInTokenCommand(oldTokenValue);

        when(signInTokenRepository.findByToken(oldTokenValue)).thenReturn(oldToken);
        when(signInTokenRepository.save(any(SignInToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        final SignInToken newToken = signInTokenService.refresh(command);

        // then
        final ArgumentCaptor<SignInToken> tokenCaptor = ArgumentCaptor.forClass(SignInToken.class);
        verify(signInTokenRepository, times(2)).save(tokenCaptor.capture());

        // 첫 번째 save: 기존 토큰 revoke
        final SignInToken revokedToken = tokenCaptor.getAllValues().get(0);
        assertThat(revokedToken.getDeletedAt()).isNotNull();
        assertThat(revokedToken.isRevoked()).isTrue();

        // 두 번째 save: 새 토큰 생성
        final SignInToken createdToken = tokenCaptor.getAllValues().get(1);
        assertThat(createdToken.getUserId()).isEqualTo(oldToken.getUserId());
        assertThat(createdToken.getSignInId()).isEqualTo(oldToken.getSignInId());
        assertThat(createdToken.getToken()).isNotEqualTo(oldTokenValue); // 새로운 토큰 값
        assertThat(createdToken.isValid()).isTrue();
        assertThat(createdToken.getDeletedAt()).isNull();

        // 반환된 토큰은 새로 생성된 토큰
        assertThat(newToken).isEqualTo(createdToken);
    }
}