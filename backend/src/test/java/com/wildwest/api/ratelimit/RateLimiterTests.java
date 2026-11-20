package com.wildwest.api.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTests {

    /** A clock the test moves by hand. */
    static final class TestClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T00:00:00Z");
        void advance(Duration d) { now = now.plus(d); }
        @Override public Instant instant() { return now; }
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
    }

    private final TestClock clock = new TestClock();
    private final RateLimiter limiter = new RateLimiter(clock);
    private final Duration window = Duration.ofMinutes(5);

    @Test
    void allowsUpToTheLimitThenRejectsWithRetryAfter() {
        for (int i = 0; i < 3; i++) {
            assertThat(limiter.tryAcquire("login:1.2.3.4", 3, window).allowed()).isTrue();
        }
        clock.advance(Duration.ofMinutes(2));

        RateLimiter.Decision rejected = limiter.tryAcquire("login:1.2.3.4", 3, window);
        assertThat(rejected.allowed()).isFalse();
        assertThat(rejected.retryAfter()).isEqualTo(Duration.ofMinutes(3));
    }

    @Test
    void windowResetsAfterItEnds() {
        for (int i = 0; i < 4; i++) limiter.tryAcquire("login:1.2.3.4", 3, window);
        clock.advance(window);
        assertThat(limiter.tryAcquire("login:1.2.3.4", 3, window).allowed()).isTrue();
    }

    @Test
    void keysAreCountedSeparately() {
        for (int i = 0; i < 4; i++) limiter.tryAcquire("login:1.2.3.4", 3, window);
        assertThat(limiter.tryAcquire("login:5.6.7.8", 3, window).allowed()).isTrue();
        assertThat(limiter.tryAcquire("register:1.2.3.4", 3, window).allowed()).isTrue();
    }

    @Test
    void oldWindowsAreEvicted() {
        limiter.tryAcquire("a", 3, window);
        clock.advance(Duration.ofHours(2));
        limiter.tryAcquire("b", 3, window);

        limiter.evictOlderThan(Duration.ofHours(1));
        assertThat(limiter.size()).isEqualTo(1);
    }
}
