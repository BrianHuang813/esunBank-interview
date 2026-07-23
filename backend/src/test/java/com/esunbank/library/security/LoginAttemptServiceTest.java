package com.esunbank.library.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.esunbank.library.common.exception.ApiException;
import com.esunbank.library.common.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginAttemptServiceTest {

    private static final String PHONE = "0912345678";
    private static final String CLIENT_IP = "192.0.2.10";

    private MutableClock clock;
    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-07-24T00:00:00Z"));
        service = new LoginAttemptService(
                new LoginProtectionProperties(
                        3,
                        Duration.ofMinutes(15),
                        Duration.ofMinutes(10),
                        3,
                        Duration.ofMinutes(1),
                        100
                ),
                clock
        );
    }

    @Test
    void locksAccountAfterConfiguredFailuresAndUnlocksAfterDuration() {
        service.recordFailure(PHONE, "192.0.2.11");
        service.recordFailure(PHONE, "192.0.2.12");
        service.recordFailure(PHONE, "192.0.2.13");

        assertThatThrownBy(() -> service.checkAllowed(PHONE, "192.0.2.14"))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS));

        clock.advance(Duration.ofMinutes(10));

        assertThatCode(() -> service.checkAllowed(PHONE, "192.0.2.14"))
                .doesNotThrowAnyException();

        service.recordFailure(PHONE, "192.0.2.14");
        service.recordFailure(PHONE, "192.0.2.15");

        assertThatCode(() -> service.checkAllowed(PHONE, "192.0.2.16"))
                .doesNotThrowAnyException();
    }

    @Test
    void limitsFailuresFromOneIpAcrossDifferentAccounts() {
        service.recordFailure("0911111111", CLIENT_IP);
        service.recordFailure("0922222222", CLIENT_IP);
        service.recordFailure("0933333333", CLIENT_IP);

        assertThatThrownBy(() -> service.checkAllowed("0944444444", CLIENT_IP))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS));

        clock.advance(Duration.ofMinutes(1));

        assertThatCode(() -> service.checkAllowed("0944444444", CLIENT_IP))
                .doesNotThrowAnyException();
    }

    @Test
    void successfulLoginClearsAccountFailures() {
        service.recordFailure(PHONE, "192.0.2.11");
        service.recordFailure(PHONE, "192.0.2.12");
        service.recordSuccess(PHONE);
        service.recordFailure(PHONE, "192.0.2.13");
        service.recordFailure(PHONE, "192.0.2.14");

        assertThatCode(() -> service.checkAllowed(PHONE, "192.0.2.15"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsNewTrackingKeysWhenCapacityIsExhausted() {
        LoginAttemptService boundedService = new LoginAttemptService(
                new LoginProtectionProperties(
                        3,
                        Duration.ofMinutes(15),
                        Duration.ofMinutes(10),
                        3,
                        Duration.ofMinutes(1),
                        1
                ),
                clock
        );
        boundedService.recordFailure("0911111111", "192.0.2.11");

        assertThatThrownBy(() ->
                boundedService.recordFailure("0922222222", "192.0.2.12"))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS));
    }

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
