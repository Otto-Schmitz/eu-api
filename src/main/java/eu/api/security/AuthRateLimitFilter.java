package eu.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.api.dto.response.ErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limits requests to /api/v1/auth/** by client IP.
 * Returns 429 Too Many Requests with JSON body when limit exceeded.
 */
@Slf4j
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final String AUTH_PATH_PREFIX = "/api/v1/auth/";
    private static final String TRACE_ID_ATTRIBUTE = "traceId";

    private final ObjectMapper objectMapper;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int requestsPerMinute;
    private final int burst;

    public AuthRateLimitFilter(ObjectMapper objectMapper,
                               @Value("${rate-limit.auth.requests-per-minute:10}") int requestsPerMinute,
                               @Value("${rate-limit.auth.burst:5}") int burst) {
        this.objectMapper = objectMapper;
        this.requestsPerMinute = requestsPerMinute;
        this.burst = burst;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith(AUTH_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String clientKey = resolveClientKey(request);
        Bucket bucket = buckets.computeIfAbsent(clientKey, k -> createBucket());
        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for client key prefix");
            writeRateLimitResponse(request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(requestsPerMinute, Refill.intervally(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private void writeRateLimitResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String traceId = request.getAttribute(TRACE_ID_ATTRIBUTE) != null
                ? request.getAttribute(TRACE_ID_ATTRIBUTE).toString()
                : null;
        ErrorResponse body = ErrorResponse.builder()
                .code("RATE_LIMIT_EXCEEDED")
                .message("Too many requests. Try again later.")
                .traceId(traceId)
                .build();
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
