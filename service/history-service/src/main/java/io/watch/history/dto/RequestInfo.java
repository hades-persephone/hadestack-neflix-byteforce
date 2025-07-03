package io.watch.history.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

@Getter
public class RequestInfo {
    private final String ip;
    private final String userAgent;
    private final String method;
    private final String uri;
    private final Instant timestamp;
    private final String contentType;
    private final Long contentLength;
    private final Map<String, String> headers;
    private final Map<String, String[]> parameters;
    private final Map<String, String> securityHeaders;
    private final String deviceInfo;
    private final String geoInfo;

    // Private constructor for builder
    private RequestInfo(Builder builder) {
        this.ip = builder.ip;
        this.userAgent = builder.userAgent;
        this.method = builder.method;
        this.uri = builder.uri;
        this.timestamp = builder.timestamp;
        this.contentType = builder.contentType;
        this.contentLength = builder.contentLength;
        this.headers = builder.headers;
        this.parameters = builder.parameters;
        this.securityHeaders = builder.securityHeaders;
        this.deviceInfo = builder.deviceInfo;
        this.geoInfo = builder.geoInfo;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static RequestInfo empty() {
        return new Builder().build();
    }

    // Builder class
    public static class Builder {
        private String ip;
        private String userAgent;
        private String method;
        private String uri;
        private Instant timestamp;
        private String contentType;
        private Long contentLength;
        private Map<String, String> headers;
        private Map<String, String[]> parameters;
        private Map<String, String> securityHeaders;
        private String deviceInfo;
        private String geoInfo;

        public Builder ip(String ip) { this.ip = ip; return this; }
        public Builder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
        public Builder method(String method) { this.method = method; return this; }
        public Builder uri(String uri) { this.uri = uri; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public Builder contentType(String contentType) { this.contentType = contentType; return this; }
        public Builder contentLength(Long contentLength) { this.contentLength = contentLength; return this; }
        public Builder headers(Map<String, String> headers) { this.headers = headers; return this; }
        public Builder parameters(Map<String, String[]> parameters) { this.parameters = parameters; return this; }
        public Builder securityHeaders(Map<String, String> securityHeaders) { this.securityHeaders = securityHeaders; return this; }
        public Builder deviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; return this; }
        public Builder geoInfo(String geoInfo) { this.geoInfo = geoInfo; return this; }

        public RequestInfo build() {
            return new RequestInfo(this);
        }
    }

}
