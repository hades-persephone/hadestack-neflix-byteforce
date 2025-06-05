package io.watch.search.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationError {
    private String field;
    private Object rejectedValue;
    private String message;
}