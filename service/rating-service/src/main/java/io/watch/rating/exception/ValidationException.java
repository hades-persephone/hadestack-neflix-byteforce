package io.watch.rating.exception;

import lombok.Getter;

@Getter
public class ValidationException extends RatingServiceException {
    private final String field;
    private final Object rejectedValue;

    public ValidationException(String message, String field, Object rejectedValue) {
        super(message, "VALIDATION_ERROR");
        this.field = field;
        this.rejectedValue = rejectedValue;
    }

    public ValidationException(String message, String field, Object rejectedValue, Throwable cause) {
        super(message, "VALIDATION_ERROR", cause);
        this.field = field;
        this.rejectedValue = rejectedValue;
    }
}