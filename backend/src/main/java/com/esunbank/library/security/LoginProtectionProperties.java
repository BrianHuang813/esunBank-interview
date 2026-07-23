package com.esunbank.library.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.login-protection")
public record LoginProtectionProperties(
        int accountMaxFailures,
        Duration accountWindow,
        Duration accountLockDuration,
        int ipMaxFailures,
        Duration ipWindow,
        int maxTrackedKeys
) {

    public LoginProtectionProperties {
        requirePositive(accountMaxFailures, "account-max-failures");
        requirePositive(ipMaxFailures, "ip-max-failures");
        requirePositive(maxTrackedKeys, "max-tracked-keys");
        requirePositive(accountWindow, "account-window");
        requirePositive(accountLockDuration, "account-lock-duration");
        requirePositive(ipWindow, "ip-window");
    }

    private static void requirePositive(int value, String name) {
        if (value < 1) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    private static void requirePositive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}
