package io.watch.rating.exception;

public class RatingNotFoundException extends RatingServiceException {
    public RatingNotFoundException(String message) {
        super(message, "RATING_NOT_FOUND");
    }

    public RatingNotFoundException(String message, Throwable cause) {
        super(message, "RATING_NOT_FOUND", cause);
    }
}