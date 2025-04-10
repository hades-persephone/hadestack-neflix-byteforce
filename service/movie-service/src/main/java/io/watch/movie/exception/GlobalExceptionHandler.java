package io.watch.movie.exception;

import io.watch.movie.response.ResponseBuilder;
import io.watch.movie.response.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private ResponseBuilder responseBuilder;

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<ApiResponseEntity<Object>> handleMovieNotFound(MovieNotFoundException ex, HttpServletRequest request) {
        return responseBuilder.error("MOVIE_NOT_FOUND", ex.getMessage(), null, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseEntity<Object>> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<String> errorMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return responseBuilder.error(
                "VALIDATION_ERROR",
                "Validation failed",
                errorMessages,
                request,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseEntity<Object>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        BindingResult result = ex.getBindingResult();
        FieldError fieldError = result.getFieldError();
        Map<String, Object> details = new HashMap<>();
        if (fieldError != null) {
            details.put("field", fieldError.getField());
            details.put("value", fieldError.getRejectedValue());
        }
        return responseBuilder.error("INVALID_INPUT", "Validation failed", details, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseEntity<Object>> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return responseBuilder.error("BAD_REQUEST", "Malformed JSON request", null, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseEntity<Object>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return responseBuilder.error("ACCESS_DENIED", "You do not have permission", null, request, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponseEntity<Object>> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        return responseBuilder.error("NOT_FOUND", "API endpoint not found", null, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CustomAppException.class)
    public ResponseEntity<ApiResponseEntity<Object>> handleCustomAppException(CustomAppException ex, HttpServletRequest request) {
        return responseBuilder.error(ex.getCode(), ex.getMessage(), ex.getDetails(), request, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseEntity<Object>> handleOtherExceptions(Exception ex, HttpServletRequest request) {
        ex.printStackTrace();
        return responseBuilder.error("INTERNAL_SERVER_ERROR", "Unexpected error occurred", null, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
