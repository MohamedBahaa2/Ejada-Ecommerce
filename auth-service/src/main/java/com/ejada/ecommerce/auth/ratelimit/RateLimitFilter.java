package com.ejada.ecommerce.auth.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

/**
  /login and /register per client. Everything else passes straight through.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final Set<String> PROTECTED_PATHS =
            Set.of("/api/v1/auth/login", "/api/v1/auth/register");

    private final InMemoryRateLimiter limiter = new InMemoryRateLimiter(10, Duration.ofMinutes(1));

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !PROTECTED_PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String key = clientKey(request) + ":" + request.getRequestURI();

        if (limiter.tryAcquire(key)) {
            chain.doFilter(request, response);
            return;
        }

        long retryAfter = limiter.retryAfterSeconds(key);
        log.warn("Rate limit hit for {} on {}", clientKey(request), request.getRequestURI());

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfter));
        response.getWriter().write("""
                {"type":"about:blank","title":"Too Many Requests","status":429,\
                "detail":"Too many attempts. Try again in %d seconds."}"""
                .formatted(retryAfter));
    }

    /**
     * Behind the gateway every request arrives from one socket, so the peer address would
     * bucket the entire internet together. X-Forwarded-For is only trustworthy because
     * nothing but the gateway can reach this port — the same assumption /me relies on.
     * If that stops holding, this header is trivially spoofable and the limiter becomes
     * decorative.
     */
    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].strip();
        }
        return request.getRemoteAddr();
    }

    @Scheduled(fixedDelay = 300_000)
    void evictStaleWindows() {
        limiter.evictStale();
    }
}