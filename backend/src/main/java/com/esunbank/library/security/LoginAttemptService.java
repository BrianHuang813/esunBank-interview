package com.esunbank.library.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esunbank.library.common.exception.ApiException;
import com.esunbank.library.common.exception.ErrorCode;

@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);
    private static final int CLEANUP_INTERVAL = 256;

    private final LoginProtectionProperties properties;
    private final Clock clock;
    private final Map<String, AccountAttempt> accountAttempts = new ConcurrentHashMap<>();
    private final Map<String, WindowAttempt> ipAttempts = new ConcurrentHashMap<>();
    private final AtomicLong operationCount = new AtomicLong();

    @Autowired
    public LoginAttemptService(LoginProtectionProperties properties) {
        this(properties, Clock.systemUTC());
    }

    LoginAttemptService(LoginProtectionProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public void checkAllowed(String phoneNumber, String clientIp) {
        Instant now = clock.instant();
        cleanupPeriodically(now);

        AccountAttempt accountAttempt = accountAttempts.get(phoneNumber);
        if (accountAttempt != null
                && accountAttempt.lockedUntil() != null
                && now.isBefore(accountAttempt.lockedUntil())) {
            log.debug(
                    "Login blocked reason=account_locked phone={} clientIp={}",
                    maskPhone(phoneNumber),
                    normalizeClientIp(clientIp)
            );
            throw new ApiException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
        }

        String normalizedIp = normalizeClientIp(clientIp);
        WindowAttempt ipAttempt = ipAttempts.get(normalizedIp);
        if (ipAttempt != null
                && isWithinWindow(ipAttempt.windowStartedAt(), properties.ipWindow(), now)
                && ipAttempt.failures() >= properties.ipMaxFailures()) {
            log.debug(
                    "Login blocked reason=ip_rate_limit phone={} clientIp={}",
                    maskPhone(phoneNumber),
                    normalizedIp
            );
            throw new ApiException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
        }
    }

    public void recordFailure(String phoneNumber, String clientIp) {
        Instant now = clock.instant();
        ensureCapacity(now, phoneNumber, normalizeClientIp(clientIp));

        AccountAttempt accountAttempt = accountAttempts.compute(phoneNumber, (key, current) -> {
            boolean reset = current == null
                    || !isWithinWindow(current.windowStartedAt(), properties.accountWindow(), now)
                    || (current.lockedUntil() != null && !now.isBefore(current.lockedUntil()));
            int failures = reset
                    ? 1
                    : current.failures() + 1;
            Instant windowStartedAt = failures == 1 ? now : current.windowStartedAt();
            Instant lockedUntil = failures >= properties.accountMaxFailures()
                    ? now.plus(properties.accountLockDuration())
                    : null;
            return new AccountAttempt(failures, windowStartedAt, lockedUntil);
        });

        String normalizedIp = normalizeClientIp(clientIp);
        WindowAttempt ipAttempt = ipAttempts.compute(normalizedIp, (key, current) -> {
            if (current == null || !isWithinWindow(current.windowStartedAt(), properties.ipWindow(), now)) {
                return new WindowAttempt(1, now);
            }
            return new WindowAttempt(current.failures() + 1, current.windowStartedAt());
        });

        log.warn(
                "Login failed phone={} clientIp={} accountFailures={} accountLocked={} ipFailures={} ipLimited={}",
                maskPhone(phoneNumber),
                normalizedIp,
                accountAttempt.failures(),
                accountAttempt.lockedUntil() != null,
                ipAttempt.failures(),
                ipAttempt.failures() >= properties.ipMaxFailures()
        );
    }

    public void recordSuccess(String phoneNumber) {
        accountAttempts.remove(phoneNumber);
    }

    private void ensureCapacity(Instant now, String phoneNumber, String clientIp) {
        if ((!accountAttempts.containsKey(phoneNumber) && accountAttempts.size() >= properties.maxTrackedKeys())
                || (!ipAttempts.containsKey(clientIp) && ipAttempts.size() >= properties.maxTrackedKeys())) {
            cleanupExpired(now);
        }
        if ((!accountAttempts.containsKey(phoneNumber) && accountAttempts.size() >= properties.maxTrackedKeys())
                || (!ipAttempts.containsKey(clientIp) && ipAttempts.size() >= properties.maxTrackedKeys())) {
            log.debug("Login blocked reason=protection_capacity clientIp={}", clientIp);
            throw new ApiException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
        }
    }

    private void cleanupPeriodically(Instant now) {
        if (operationCount.incrementAndGet() % CLEANUP_INTERVAL == 0) {
            cleanupExpired(now);
        }
    }

    private void cleanupExpired(Instant now) {
        accountAttempts.entrySet().removeIf(entry -> {
            AccountAttempt attempt = entry.getValue();
            Instant expiresAt = attempt.windowStartedAt().plus(properties.accountWindow());
            if (attempt.lockedUntil() != null && attempt.lockedUntil().isAfter(expiresAt)) {
                expiresAt = attempt.lockedUntil();
            }
            return !now.isBefore(expiresAt);
        });
        ipAttempts.entrySet().removeIf(entry ->
                !isWithinWindow(entry.getValue().windowStartedAt(), properties.ipWindow(), now));
    }

    private boolean isWithinWindow(Instant startedAt, Duration window, Instant now) {
        return now.isBefore(startedAt.plus(window));
    }

    private String normalizeClientIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return "unknown";
        }
        String trimmed = clientIp.trim();
        return trimmed.length() <= 128 ? trimmed : trimmed.substring(0, 128);
    }

    private String maskPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "***";
        }
        return phoneNumber.substring(0, 2)
                + "*".repeat(phoneNumber.length() - 4)
                + phoneNumber.substring(phoneNumber.length() - 2);
    }

    private record AccountAttempt(int failures, Instant windowStartedAt, Instant lockedUntil) {
    }

    private record WindowAttempt(int failures, Instant windowStartedAt) {
    }
}
