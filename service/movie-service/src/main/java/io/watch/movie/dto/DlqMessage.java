package io.watch.movie.handler;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class DlqMessage {
    private String originalTopic;
    private String originalKey;
    private String originalMessage;
    private String errorType;
    private String errorMessage;
    private Integer retryCount;
    private Map<String, Object> headers;
    private LocalDateTime timestamp;
}
