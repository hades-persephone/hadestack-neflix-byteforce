package io.watch.movie.response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component("movieResponseBuilder")
@Slf4j
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
        String requestId = Optional.ofNullable(request.getHeader("X-Request-ID"))
                .filter(StringUtils::hasText)
                .orElse(UUID.randomUUID().toString());

        Map<String, Object> paramMap = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().length == 1 ? e.getValue()[0] : Arrays.asList(e.getValue())
                ));

        Map<String, Object> requestBodyMap = new HashMap<>();
        try {
            String requestBody = getRequestBody(request);
            if (StringUtils.hasText(requestBody) && !"\"\"".equals(requestBody.trim())) {
                requestBodyMap = new Gson().fromJson(requestBody, Map.class);
            }
        } catch (IOException e) {
             log.error("Failed to read request body", e);
        }

        return new Meta(requestId, path, paramMap, requestBodyMap);
    }

    private String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder body = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        return body.toString();
    }
}
