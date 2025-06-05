package io.watch.search.exception;


import io.watch.search.repsonse.MetaResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<MetaResponse<Object>> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.error("Resource not found: {}", ex.getMessage());

        MetaResponse<Object> response = MetaResponse.error(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<MetaResponse<Object>> handleBadRequest(
            BadRequestException ex, HttpServletRequest request) {

        log.error("Bad request: {}", ex.getMessage());

        MetaResponse<Object> response = MetaResponse.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<MetaResponse<Object>> handleResourceAlreadyExists(
            ResourceAlreadyExistsException ex, HttpServletRequest request) {

        log.error("Resource already exists: {}", ex.getMessage());

        MetaResponse<Object> response = MetaResponse.error(
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MetaResponse<Object>> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.error("Validation failed: {}", ex.getMessage());

        List<ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationError(
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        MetaResponse<Object> response = MetaResponse.error(
                "Validation failed",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                validationErrors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<MetaResponse<Object>> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        log.error("Constraint violation: {}", ex.getMessage());

        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());

        MetaResponse<Object> response = MetaResponse.error(
                "Constraint violation",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                errors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<MetaResponse<Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        log.error("Method not supported: {}", ex.getMessage());

        String supportedMethods = String.join(", ", ex.getSupportedMethods());
        String message = String.format("Method '%s' not supported. Supported methods: %s",
                ex.getMethod(), supportedMethods);

        MetaResponse<Object> response = MetaResponse.error(
                message,
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<MetaResponse<Object>> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

        log.error("Media type not supported: {}", ex.getMessage());

        String supportedTypes = ex.getSupportedMediaTypes()
                .stream()
                .map(MediaType::toString)
                .collect(Collectors.joining(", "));

        String message = String.format("Media type '%s' not supported. Supported types: %s",
                ex.getContentType(), supportedTypes);

        MetaResponse<Object> response = MetaResponse.error(
                message,
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<MetaResponse<Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.error("Data integrity violation: {}", ex.getMessage());

        String message = "Data integrity violation occurred";
        if (ex.getCause() instanceof ConstraintViolationException) {
            message = "Database constraint violation";
        }

        MetaResponse<Object> response = MetaResponse.error(
                message,
                HttpStatus.CONFLICT.value(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MetaResponse<Object>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        log.error("Access denied: {}", ex.getMessage());

        MetaResponse<Object> response = MetaResponse.error(
                "Access denied. You don't have permission to access this resource",
                HttpStatus.FORBIDDEN.value(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MetaResponse<Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error occurred: ", ex);

        MetaResponse<Object> response = MetaResponse.error(
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}