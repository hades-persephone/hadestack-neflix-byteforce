package io.watch.movie.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class Meta {
    private String requestId;
    private String timestamp;
    private String path;

    public Meta(String requestId, String path) {
        this.requestId = requestId;
        this.timestamp = Instant.now().toString();
        this.path = path;
    }
}
