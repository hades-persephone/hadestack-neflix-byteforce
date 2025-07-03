package io.watch.movie.exception;

public class MovieNotFoundException extends RuntimeException {
    public MovieNotFoundException(String s) {
        super(s);
    }
    public MovieNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
