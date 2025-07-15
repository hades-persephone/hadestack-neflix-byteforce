package io.watch.rating.exception;


import lombok.Getter;

@Getter
public class RatingServiceException extends RuntimeException {
    private final String errorCode;
    private final Object details;

    public RatingServiceException(String message) {
        super(message);
        this.errorCode = "RATING_SERVICE_ERROR";
        this.details = null;
    }

    public RatingServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "RATING_SERVICE_ERROR";
        this.details = null;
    }

    public RatingServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    public RatingServiceException(String message, String errorCode, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public RatingServiceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = null;
    }
}