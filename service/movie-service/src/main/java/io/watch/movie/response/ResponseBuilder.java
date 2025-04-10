package io.watch.movie.response;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ResponseBuilder {
    public <T> ResponseEntity<ApiResponseEntity<T>> success(T data, HttpServletRequest request) {
        return ResponseEntity.ok(
                new ApiResponseEntity<T>() {{
                    setStatus("success");
                    setData(data);
                    setMeta(buildMeta(request));
                }}
        );
    }

    public ResponseEntity<ApiResponseEntity<Object>> successMessage(String message, HttpServletRequest request) {
        return ResponseEntity.ok(
                new ApiResponseEntity<>() {{
                    setStatus("success");
                    setMessage(message);
                    setMeta(buildMeta(request));
                }}
        );
    }

    // Warning
    public ResponseEntity<ApiResponseEntity<Object>> warning(String message, Object details, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponseEntity<>() {{
                    setStatus("warning");
                    setMessage(message);
                    setData(details);
                    setMeta(buildMeta(request));
                }}
        );
    }

    public ResponseEntity<ApiResponseEntity<Object>> error(String code, String message, Object details, HttpServletRequest request, HttpStatus status) {
        return ResponseEntity.status(status).body(
                new ApiResponseEntity<>() {{
                    setStatus("error");
                    setError(new ErrorDetail(code, message, details));
                    setMeta(buildMeta(request));
                }}
        );
    }

    private Meta buildMeta(HttpServletRequest request) {
        String path = request.getRequestURI();
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        return new Meta(requestId, path);
    }
}
