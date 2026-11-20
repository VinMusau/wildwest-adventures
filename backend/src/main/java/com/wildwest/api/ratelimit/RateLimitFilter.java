package com.wildwest.api.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.jspecify.annotations.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.List;

/**
 * Per-IP limits on the endpoints that are worth abusing: guessing passwords, creating accounts
 * and creating bookings (each booking holds seats). Runs before Spring Security so rejected
 * requests are cheap.
 *
 * <p>The client is identified by {@code request.getRemoteAddr()}. Behind a reverse proxy or load
 * balancer, set {@code server.forward-headers-strategy} so that is the real client address, and
 * make sure the proxy overwrites (not appends to) {@code X-Forwarded-For}.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    record Rule(String name, String method, String path, int limit, Duration window, String message) {}

    static final List<Rule> RULES = List.of(
            new Rule("login", "POST", "/api/v1/auth/login", 10, Duration.ofMinutes(5),
                    "Too many sign-in attempts. Please wait a few minutes and try again."),
            new Rule("register", "POST", "/api/v1/auth/register", 5, Duration.ofHours(1),
                    "Too many accounts created from this network. Please try again later."),
            new Rule("booking", "POST", "/api/v1/bookings", 20, Duration.ofHours(1),
                    "Too many booking attempts. Please try again later.")
    );

    private static final Duration LONGEST_WINDOW = RULES.stream().map(Rule::window).max(Duration::compareTo).orElseThrow();

    private final RateLimiter limiter = new RateLimiter(Clock.systemUTC());
    private final boolean enabled;

    public RateLimitFilter(@Value("${wildwest.rate-limit.enabled:true}") boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !enabled || ruleFor(request) == null;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {
        Rule rule = ruleFor(request);
        String client = request.getRemoteAddr();
        RateLimiter.Decision decision = limiter.tryAcquire(rule.name() + ":" + client, rule.limit(), rule.window());
        if (decision.allowed()) {
            chain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1, (decision.retryAfter().toMillis() + 999) / 1000);
        log.warn("Rate limit '{}' hit by {}", rule.name(), client);
        response.setStatus(429);
        response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        // message is one of the constants above, so no JSON escaping is needed.
        response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"Too Many Requests\",\"status\":429,"
                + "\"detail\":\"" + rule.message() + "\",\"retryAfterSeconds\":" + retryAfterSeconds + "}");
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    void evictOldWindows() {
        limiter.evictOlderThan(LONGEST_WINDOW);
    }

    private static Rule ruleFor(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (Rule r : RULES) {
            if (r.method().equals(request.getMethod()) && r.path().equals(path)) {
                return r;
            }
        }
        return null;
    }
}
