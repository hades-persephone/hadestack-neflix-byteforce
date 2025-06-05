package io.watch.search.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ElasticsearchBusinessException extends RuntimeException {
    private final HttpStatus status;

    public ElasticsearchBusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public ElasticsearchBusinessException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

}
