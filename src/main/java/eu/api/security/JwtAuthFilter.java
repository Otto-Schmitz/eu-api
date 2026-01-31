package eu.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.api.dto.response.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * Extracts Bearer token from Authorization header, verifies via JwtService,
 * and sets SecurityContext with AuthPrincipal for user context propagation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TRACE_ID_ATTRIBUTE = "traceId";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            UUID userId = jwtService.verifyAccessToken(token);
            AuthPrincipal principal = AuthPrincipal.of(userId);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.debug("Access token expired");
            SecurityContextHolder.clearContext();
            writeUnauthorized(response, request, "TOKEN_EXPIRED", "Access token has expired");
        } catch (Exception e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            writeUnauthorized(response, request, "INVALID_TOKEN", "Invalid access token");
        }
    }

    private void writeUnauthorized(HttpServletResponse response, HttpServletRequest request,
                                   String code, String message) throws IOException {
        String traceId = Optional.ofNullable(request.getAttribute(TRACE_ID_ATTRIBUTE))
                .map(Object::toString)
                .orElse(null);
        ErrorResponse body = ErrorResponse.builder()
                .code(code)
                .message(message)
                .traceId(traceId)
                .build();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
