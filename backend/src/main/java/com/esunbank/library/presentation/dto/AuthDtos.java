package com.esunbank.library.presentation.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "手機號碼不可空白")
            @Pattern(regexp = "^09[0-9]{8}$", message = "手機號碼格式不正確")
            String phoneNumber,

            @NotBlank(message = "密碼不可空白")
            @Size(min = 8, max = 20, message = "密碼長度必須介於 8 到 20 個字元")
            String password,

            @NotBlank(message = "使用者名稱不可空白")
            @Size(max = 30, message = "使用者名稱最多 30 個字元")
            @Pattern(
                    regexp = "^[\\p{IsHan}A-Za-z ]+$",
                    message = "使用者名稱只能包含中文、英文字母與空格"
            )
            String userName
    ) {
    }

    public record LoginRequest(
            @NotBlank(message = "手機號碼不可空白")
            @Pattern(regexp = "^09[0-9]{8}$", message = "手機號碼格式不正確")
            String phoneNumber,

            @NotBlank(message = "密碼不可空白")
            @Size(max = 20, message = "密碼最多 20 個字元")
            String password
    ) {
    }

    public record UserResponse(
            long id,
            String phoneNumber,
            String userName,
            OffsetDateTime registrationTime,
            OffsetDateTime lastLoginTime
    ) {
    }

    public record LoginUserResponse(long id, String userName) {
    }

    public record LoginResponse(
            String accessToken,
            String tokenType,
            long expiresIn,
            LoginUserResponse user
    ) {
    }
}
