package com.wildwest.api.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fixed-window request counter, kept in memory. Enough for a single server; with several
 * instances behind a load balancer each would count separately (move this to Redis then).
 */
public class RateLimiter {

    /** Result of one attempt: allowed, or how long until the window resets. */
    public record Decision(boolean allowed, Duration retryAfter) {}

    private record Window(long startMillis, int count) {}

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    public RateLimiter(Clock clock) {
        this.clock = clock;
    }

    public Decision tryAcquire(String key, int limit, Duration window) {
        long now = clock.millis();
        long windowMillis = window.toMillis();
        Window w = windows.compute(
                key,
                (k, current) -> current == null || now - current.startMillis() >= windowMillis
                        ? new Window(now, 1)
                        : new Window(current.startMillis(), current.count() + 1));
        if (w.count() <= limit) {
            return new Decision(true, Duration.ZERO);
        }
        return new Decision(false, Duration.ofMillis(w.startMillis() + windowMillis - now));
    }

    /** Drops windows that ended at least {@code maxWindow} ago, so memory doesn't grow forever. */
    public void evictOlderThan(Duration maxWindow) {
        long cutoff = clock.millis() - maxWindow.toMillis();
        windows.values().removeIf(w -> w.startMillis() < cutoff);
    }

    int size() {
        return windows.size();
    }
}
