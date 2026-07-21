package com.esunbank.library.business.model;

public record AuthResult(String accessToken, long expiresIn, User user) {
}
