package io.watch.history.exception;

public class CassandraPersistenceException extends RuntimeException {

    public CassandraPersistenceException(String message) {
        super(message);
    }

    public CassandraPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CassandraPersistenceException(Throwable cause) {
        super(cause);
    }
}