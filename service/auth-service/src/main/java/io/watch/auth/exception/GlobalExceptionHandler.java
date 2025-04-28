package io.watch.auth.exception;

import io.watch.auth.response.ApiResponseEntity;
import io.watch.auth.response.ResponseBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import reactor.core.publisher.Mono;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private ResponseBuilder responseBuilder;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseEntity<ApiResponseEntity<Object>>> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errorMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return Mono.just(responseBuilder.error(
                "VALIDATION_ERROR",
                "Validation failed",
                errorMessages,
                request,
                HttpStatus.BAD_REQUEST
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseEntity<ApiResponseEntity<Object>>> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return Mono.just(responseBuilder.error(
                "BAD_REQUEST",
                "Malformed JSON request",
                null,
                request,
                HttpStatus.BAD_REQUEST
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Mono<ResponseEntity<ApiResponseEntity<Object>>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return Mono.just(responseBuilder.error(
                "ACCESS_DENIED",
                "You do not have permission",
                null,
                request,
                HttpStatus.FORBIDDEN
        ));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<ApiResponseEntity<Object>>> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        return Mono.just(responseBuilder.error(
                "NOT_FOUND",
                "API endpoint not found",
                null,
                request,
                HttpStatus.NOT_FOUND
        ));
    }

    @ExceptionHandler(CustomAppException.class)
    public Mono<ResponseEntity<ApiResponseEntity<Object>>> handleCustomAppException(CustomAppException ex, HttpServletRequest request) {
        return Mono.just(responseBuilder.error(
                ex.getCode(),
                ex.getMessage(),
                ex.getDetails(),
                request,
                ex.getStatus()
        ));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ResponseEntity<ApiResponseEntity<Object>>> handleOtherExceptions(Exception ex, HttpServletRequest request) {
        ex.printStackTrace();
        return Mono.just(responseBuilder.error(
                "INTERNAL_SERVER_ERROR",
                "Unexpected error occurred",
                null,
                request,
                HttpStatus.INTERNAL_SERVER_ERROR
        ));
    }
}
