package com.ejada.ecommerce.auth.ratelimit;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixed-window counter keyed by client + endpoint.
 *
 * Documented limitation (A.7.2): the counters live in this JVM's heap. Two instances
 * behind a load balancer double the effective limit, and a restart resets every counter.
 * Acceptable for a single-instance project; in production this belongs in Redis or at
 * the gateway, where it can be enforced once for the whole platform.
 */
public class InMemoryRateLimiter {

    private final int maxRequests;
    private final Duration window;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public InMemoryRateLimiter(int maxRequests, Duration window) {
        this.maxRequests = maxRequests;
        this.window = window;
    }

    public boolean tryAcquire(String key) {
        Instant now = Instant.now();
        Window current = windows.compute(key, (k, existing) ->
                (existing == null || existing.startedAt.plus(window).isBefore(now))
                        ? new Window(now)
                        : existing);
        return current.count.incrementAndGet() <= maxRequests;
    }

    public long retryAfterSeconds(String key) {
        Window current = windows.get(key);
        if (current == null) {
            return 0;
        }
        return Math.max(Duration.between(Instant.now(), current.startedAt.plus(window)).toSeconds(), 1);
    }

    /** Called on a schedule so abandoned keys do not accumulate for the life of the process. */
    public void evictStale() {
        Instant cutoff = Instant.now().minus(window);
        windows.entrySet().removeIf(e -> e.getValue().startedAt.isBefore(cutoff));
    }

    private static final class Window {
        private final Instant startedAt;
        private final AtomicInteger count = new AtomicInteger();

        private Window(Instant startedAt) {
            this.startedAt = startedAt;
        }
    }
}