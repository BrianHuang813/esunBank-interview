package com.esunbank.library.business.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.esunbank.library.business.model.AuthResult;
import com.esunbank.library.business.model.User;
import com.esunbank.library.common.exception.ApiException;
import com.esunbank.library.common.exception.ErrorCode;
import com.esunbank.library.data.repository.UserRepository;
import com.esunbank.library.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User register(String phoneNumber, String password, String userName) {
        return userRepository.create(phoneNumber, passwordEncoder.encode(password), userName);
    }

    public AuthResult login(String phoneNumber, String password) {
        User storedUser = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ApiException(ErrorCode.AUTHENTICATION_FAILED));
        if (!passwordEncoder.matches(password, storedUser.passwordHash())) {
            throw new ApiException(ErrorCode.AUTHENTICATION_FAILED);
        }

        User user = userRepository.recordLogin(storedUser.id());
        return new AuthResult(jwtService.createToken(user), jwtService.expirationSeconds(), user);
    }
}
