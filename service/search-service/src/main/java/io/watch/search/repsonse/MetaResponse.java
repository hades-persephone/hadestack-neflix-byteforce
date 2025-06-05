package io.watch.search.repsonse;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetaResponse<T> {
    private boolean success;
    private String message;
    private int status;
    private String timestamp;
    private String path;
    private T data;
    private Object errors;

    public static <T> MetaResponse<T> success(T data, String message) {
        return MetaResponse.<T>builder()
                .success(true)
                .message(message)
                .status(200)
                .timestamp(Instant.now().toString())
                .data(data)
                .build();
    }

    public static <T> MetaResponse<T> error(String message, int status, String path, Object errors) {
        return MetaResponse.<T>builder()
                .success(false)
                .message(message)
                .status(status)
                .timestamp(Instant.now().toString())
                .path(path)
                .errors(errors)
                .build();
    }
}