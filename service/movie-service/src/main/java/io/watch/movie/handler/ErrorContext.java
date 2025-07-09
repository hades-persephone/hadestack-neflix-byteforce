package io.watch.movie.handler;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@With
public class ErrorContext {
    private String originalTopic;
    private String key;
    private String message;
    private Throwable error;
    private Map<String, Object> headers;
    private Integer retryCount;
    private LocalDateTime timestamp;

    public ErrorContext withRetryCount(int retryCount) {
        return this.toBuilder().retryCount(retryCount).build();
    }

    public ErrorContext withMessage(String message) {
        return this.toBuilder().message(message).build();
    }
}
