package io.watch.search.exception;

public class ElasticsearchSearchException extends RuntimeException {
    public ElasticsearchSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
