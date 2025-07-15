package io.watch.rating.exception;

public class CacheException extends RatingServiceException {
    public CacheException(String message) {
        super(message, "CACHE_ERROR");
    }

    public CacheException(String message, Throwable cause) {
        super(message, "CACHE_ERROR", cause);
    }
}