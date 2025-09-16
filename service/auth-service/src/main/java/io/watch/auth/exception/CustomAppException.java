package io.watch.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CustomAppException extends RuntimeException {
    private final String code;
    private final Object details;
    private final HttpStatus status;

    public CustomAppException(String code, String message, Object details, HttpStatus status) {
        super(message);
        this.code = code;
        this.details = details;
        this.status = status;
    }
}
