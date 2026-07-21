package com.esunbank.library.security;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.esunbank.library.business.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void createdTokenContainsUserId() {
        var properties = new JwtProperties("test-secret-that-is-longer-than-thirty-two-bytes", 60);
        var jwtService = new JwtService(properties);
        var user = new User(
                42L,
                "0912345678",
                null,
                "王小明",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        String token = jwtService.createToken(user);

        assertThat(jwtService.parseToken(token).userId()).isEqualTo(42L);
        assertThat(jwtService.expirationSeconds()).isEqualTo(3600L);
    }
}
