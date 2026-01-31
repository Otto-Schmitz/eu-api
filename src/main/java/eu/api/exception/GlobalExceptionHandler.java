package eu.api.exception;

import eu.api.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TRACE_ID_ATTRIBUTE = "traceId";

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        String traceId = resolveTraceId(request);
        log.warn("ApiException code={} message={} traceId={}", ex.getCode(), ex.getMessage(), traceId);
        ErrorResponse body = ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .traceId(traceId)
                .details(ex instanceof ValidationException ? ((ValidationException) ex).getDetails() : null)
                .build();
        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = resolveTraceId(request);
        Map<String, Object> details = new HashMap<>();
        for (FieldError err : ex.getBindingResult().getFieldErrors()) {
            details.put(err.getField(), err.getDefaultMessage());
        }
        log.warn("Validation failed traceId={} fields={}", traceId, details.keySet());
        ErrorResponse body = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message("Invalid request")
                .details(details)
                .traceId(traceId)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex, HttpServletRequest request) {
        String traceId = resolveTraceId(request);
        log.warn("Authentication failed traceId={}", traceId);
        ErrorResponse body = ErrorResponse.builder()
                .code("UNAUTHENTICATED")
                .message("Authentication required")
                .traceId(traceId)
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex, HttpServletRequest request) {
        String traceId = resolveTraceId(request);
        log.warn("Access denied traceId={}", traceId);
        ErrorResponse body = ErrorResponse.builder()
                .code("FORBIDDEN")
                .message("Access denied")
                .traceId(traceId)
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        String traceId = resolveTraceId(request);
        log.error("Unhandled error traceId={}", traceId, ex);
        ErrorResponse body = ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .traceId(traceId)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String resolveTraceId(HttpServletRequest request) {
        return Optional.ofNullable(request.getAttribute(TRACE_ID_ATTRIBUTE))
                .map(Object::toString)
                .orElse(UUID.randomUUID().toString());
    }
}
