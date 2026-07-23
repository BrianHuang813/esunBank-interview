package com.esunbank.library.business.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.esunbank.library.business.model.User;
import com.esunbank.library.common.exception.ApiException;
import com.esunbank.library.common.exception.ErrorCode;
import com.esunbank.library.data.repository.UserRepository;
import com.esunbank.library.security.JwtService;
import com.esunbank.library.security.LoginAttemptService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private LoginAttemptService loginAttemptService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtService,
                loginAttemptService
        );
    }

    @Test
    void registerHashesPasswordBeforeSavingUser() {
        User savedUser = publicUser();
        when(passwordEncoder.encode("Interview123!")).thenReturn("bcrypt-hash");
        when(userRepository.create("0912345678", "bcrypt-hash", "王小明")).thenReturn(savedUser);

        User result = authService.register("0912345678", "Interview123!", "王小明");

        assertThat(result).isEqualTo(savedUser);
        verify(userRepository).create("0912345678", "bcrypt-hash", "王小明");
    }

    @Test
    void loginReturnsTokenAndRecordsLastLogin() {
        User storedUser = storedUser();
        User publicUser = publicUser();
        when(userRepository.findByPhoneNumber("0912345678")).thenReturn(Optional.of(storedUser));
        when(passwordEncoder.matches("Interview123!", "bcrypt-hash")).thenReturn(true);
        when(userRepository.recordLogin(1L)).thenReturn(publicUser);
        when(jwtService.createToken(publicUser)).thenReturn("signed-jwt");
        when(jwtService.expirationSeconds()).thenReturn(3600L);

        var result = authService.login("0912345678", "Interview123!", "192.0.2.10");

        assertThat(result.accessToken()).isEqualTo("signed-jwt");
        assertThat(result.expiresIn()).isEqualTo(3600L);
        assertThat(result.user()).isEqualTo(publicUser);
        verify(loginAttemptService).checkAllowed("0912345678", "192.0.2.10");
        verify(loginAttemptService).recordSuccess("0912345678");
    }

    @Test
    void loginDoesNotRevealMissingPhoneNumber() {
        when(userRepository.findByPhoneNumber("0999999999")).thenReturn(Optional.empty());
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(
                "0999999999",
                "Interview123!",
                "192.0.2.10"
        ))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(ErrorCode.AUTHENTICATION_FAILED));

        verify(passwordEncoder).matches(
                eq("Interview123!"),
                argThat(hash -> hash != null && hash.startsWith("$2"))
        );
        verify(loginAttemptService).recordFailure("0999999999", "192.0.2.10");
    }

    @Test
    void loginRejectsWrongPasswordWithSameAuthenticationError() {
        when(userRepository.findByPhoneNumber("0912345678")).thenReturn(Optional.of(storedUser()));
        when(passwordEncoder.matches("wrong-password", "bcrypt-hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(
                "0912345678",
                "wrong-password",
                "192.0.2.10"
        ))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(ErrorCode.AUTHENTICATION_FAILED));

        verify(loginAttemptService).recordFailure("0912345678", "192.0.2.10");
        verify(loginAttemptService, never()).recordSuccess("0912345678");
    }

    @Test
    void blockedLoginDoesNotQueryUserRepository() {
        doThrow(new ApiException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS))
                .when(loginAttemptService)
                .checkAllowed("0912345678", "192.0.2.10");

        assertThatThrownBy(() -> authService.login(
                "0912345678",
                "Interview123!",
                "192.0.2.10"
        )).isInstanceOfSatisfying(ApiException.class, exception ->
                assertThat(exception.errorCode()).isEqualTo(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS));

        verifyNoInteractions(userRepository);
    }

    private User storedUser() {
        return new User(
                1L,
                "0912345678",
                "bcrypt-hash",
                "王小明",
                LocalDateTime.of(2026, 7, 22, 9, 0),
                null
        );
    }

    private User publicUser() {
        return new User(
                1L,
                "0912345678",
                null,
                "王小明",
                LocalDateTime.of(2026, 7, 22, 9, 0),
                LocalDateTime.of(2026, 7, 22, 10, 0)
        );
    }
}
