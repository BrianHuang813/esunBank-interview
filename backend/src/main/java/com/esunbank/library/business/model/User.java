package com.esunbank.library.business.model;

import java.time.LocalDateTime;

public record User(
        long id,
        String phoneNumber,
        String passwordHash,
        String userName,
        LocalDateTime registrationTime,
        LocalDateTime lastLoginTime
) {
}
