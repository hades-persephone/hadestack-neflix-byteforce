package io.watch.movie.exception;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.watch.movie.response.ResponseBuilder;
import io.watch.movie.response.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final ResponseBuilder responseBuilder;

    @ExceptionHandler(MovieNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponseEntity<Object>> handleMovieNotFound(MovieNotFoundException ex, HttpServletRequest request) {
        log.warn("Movie not found: {} at {}", ex.getMessage(), request.getRequestURI());
        return responseBuilder.error("MOVIE_NOT_FOUND", ex.getMessage(), null, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponseEntity<Object>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> details = new HashMap<>();
        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put("field", error.getField());
                    errorMap.put("message", error.getDefaultMessage());
                    errorMap.put("value", String.valueOf(error.getRejectedValue()));
                    return errorMap;
                }).toList();
        details.put("fieldErrors", fieldErrors);
        log.warn("Validation failed for request {}: {} errors", request.getRequestURI(), fieldErrors.size());
        return responseBuilder.error("INVALID_INPUT", "Validation failed", details, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponseEntity<Object>> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, Object> details = new HashMap<>();
        List<Map<String, String>> violationErrors = ex.getConstraintViolations().stream()
                .map(violation -> {
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put("field", violation.getPropertyPath().toString());
                    errorMap.put("message", violation.getMessage());
                    errorMap.put("value", String.valueOf(violation.getInvalidValue()));
                    return errorMap;
                }).toList();
        details.put("violationErrors", violationErrors);
        log.warn("Constraint violation failed for request {}: {}", request.getRequestURI(), violationErrors.size());
        return responseBuilder.error("INVALID_INPUT", "Constraint violation", details, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponseEntity<Object>> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return responseBuilder.error("BAD_REQUEST", "Malformed JSON request", null, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiResponseEntity<Object>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return responseBuilder.error("ACCESS_DENIED", "You do not have permission", null, request, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponseEntity<Object>> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        return responseBuilder.error("NOT_FOUND", "API endpoint not found", null, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseEntity<Object>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Invalid argument at {}: {}", request.getRequestURI(), ex.getMessage());
        return responseBuilder.error("INVALID_ARGUMENT", ex.getMessage(), null, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseEntity<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation at {}: {}", request.getRequestURI(), ex.getMessage());
        String rootCauseMessage = getRootCauseMessage(ex);
        return responseBuilder.error(
                "DATA_INTEGRITY_VIOLATION",
                "Database operation failed due to constraint violation",
                rootCauseMessage,
                request,
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ApiResponseEntity<Object>> handleRateLimit(RequestNotPermitted ex, HttpServletRequest request) {
        log.warn("Rate limit exceeded for request: {}", request.getRequestURI());
        return responseBuilder.error("RATE_LIMIT_EXCEEDED", "Too many requests, please try again later", null, request, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ApiResponseEntity<Object>> handleCircuitBreaker(CallNotPermittedException ex, HttpServletRequest request) {
        log.warn("Circuit breaker open for request: {}", request.getRequestURI());
        return responseBuilder.error("SERVICE_UNAVAILABLE", "Service temporarily unavailable, please try again later", null, request, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(CustomAppException.class)
    public ResponseEntity<ApiResponseEntity<Object>> handleCustomAppException(CustomAppException ex, HttpServletRequest request) {
        return responseBuilder.error(ex.getCode(), ex.getMessage(), ex.getDetails(), request, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseEntity<Object>> handleOtherExceptions(Exception ex, HttpServletRequest request) {
        log.error(ex.getMessage(), ex);
        return responseBuilder.error("INTERNAL_SERVER_ERROR", "Unexpected error occurred", ex.getMessage(), request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getRootCauseMessage(Throwable throwable) {
        Throwable root = throwable;
        while(root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage();
    }

}
