package io.watch.movie.response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@AllArgsConstructor
public class Meta {
    private String requestId;
    private String timestamp;
    private String path;
    private Map<String, Object> params;
    private Map<String, Object> requestBody;

    public Meta(String requestId, String path) {
        this.requestId = requestId;
        this.timestamp = Instant.now().toString();
        this.path = path;
    }

    public Meta(String requestId, String path, Map<String, Object> params, Map<String, Object> requestBody) {
        this.requestId = requestId;
        this.path = path;
        this.params = params;
        this.requestBody = requestBody;
        this.timestamp = Instant.now().toString();
    }
}
