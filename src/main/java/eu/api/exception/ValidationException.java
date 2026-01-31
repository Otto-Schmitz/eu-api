package eu.api.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ValidationException extends ApiException {

    private final Map<String, Object> details;

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
        this.details = null;
    }

    public ValidationException(String message, Map<String, Object> details) {
        super("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
        this.details = details;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
