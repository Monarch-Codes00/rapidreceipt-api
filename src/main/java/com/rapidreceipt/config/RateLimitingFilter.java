package com.rapidreceipt.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A lightweight, zero-dependency Rate Limiting Filter that applies to all endpoints.
 * It uses a simple Token Bucket algorithm stored in memory per IP address.
 * 
 * Limit: 40 requests per minute per IP.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    private TokenBucket getBucket(String ip) {
        // 40 requests per minute
        return buckets.computeIfAbsent(ip, k -> new TokenBucket(40, 60000));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Only rate limit API endpoints, skip static files (like /uploads/)
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }

        TokenBucket bucket = getBucket(ip);

        if (bucket.tryConsume()) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Too many requests. Please try again later.\"}");
        }
    }

    /**
     * Simple thread-safe Token Bucket implementation.
     */
    private static class TokenBucket {
        private final int maxTokens;
        private final long refillIntervalMillis;
        private final AtomicInteger tokens;
        private long nextRefillTime;

        public TokenBucket(int maxTokens, long refillIntervalMillis) {
            this.maxTokens = maxTokens;
            this.refillIntervalMillis = refillIntervalMillis;
            this.tokens = new AtomicInteger(maxTokens);
            this.nextRefillTime = System.currentTimeMillis() + refillIntervalMillis;
        }

        public synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            if (now > nextRefillTime) {
                tokens.set(maxTokens);
                nextRefillTime = now + refillIntervalMillis;
            }

            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }
    }
}
