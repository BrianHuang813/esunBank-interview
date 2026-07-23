package com.esunbank.library.business.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.esunbank.library.business.model.AuthResult;
import com.esunbank.library.business.model.User;
import com.esunbank.library.common.exception.ApiException;
import com.esunbank.library.common.exception.ErrorCode;
import com.esunbank.library.data.repository.UserRepository;
import com.esunbank.library.security.JwtService;
import com.esunbank.library.security.LoginAttemptService;

@Service
public class AuthService {

    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            LoginAttemptService loginAttemptService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
    }

    public User register(String phoneNumber, String password, String userName) {
        return userRepository.create(phoneNumber, passwordEncoder.encode(password), userName);
    }

    public AuthResult login(String phoneNumber, String password, String clientIp) {
        loginAttemptService.checkAllowed(phoneNumber, clientIp);
        User storedUser = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        if (storedUser == null) {
            passwordEncoder.matches(password, DUMMY_PASSWORD_HASH);
            loginAttemptService.recordFailure(phoneNumber, clientIp);
            throw new ApiException(ErrorCode.AUTHENTICATION_FAILED);
        }
        if (!passwordEncoder.matches(password, storedUser.passwordHash())) {
            loginAttemptService.recordFailure(phoneNumber, clientIp);
            throw new ApiException(ErrorCode.AUTHENTICATION_FAILED);
        }

        loginAttemptService.recordSuccess(phoneNumber);
        User user = userRepository.recordLogin(storedUser.id());
        return new AuthResult(jwtService.createToken(user), jwtService.expirationSeconds(), user);
    }
}
