package io.watch.movie.response;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;
    private ErrorDetail error;
    private Meta meta;
}
