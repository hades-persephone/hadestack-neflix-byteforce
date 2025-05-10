package io.watch.sync.exception;

public class EventProcessingException extends RuntimeException {
    public EventProcessingException(String message) {
        super(message);
    }

    public EventProcessingException(String message, Exception e) {
        super(message, e);
    }
}
