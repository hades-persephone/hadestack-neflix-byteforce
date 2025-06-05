package io.watch.search.exception;


import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.transport.rest5_client.low_level.ResponseException;
import io.watch.search.repsonse.MetaResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalElasticsearchExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handle general Elasticsearch exceptions
     */
    @ExceptionHandler(ElasticsearchException.class)
    public ResponseEntity<MetaResponse<Object>> handleElasticsearchException(
            ElasticsearchException ex, WebRequest request) {

        String path = request.getDescription(false).replace("uri=", "");

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("type", "ElasticsearchException");
        errorDetails.put("detailed_message", ex.getMessage());

        MetaResponse<Object> response = MetaResponse.error(
                "Elasticsearch operation failed",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                path,
                errorDetails
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle Elasticsearch REST client response exceptions
     */
    @ExceptionHandler(ResponseException.class)
    public ResponseEntity<MetaResponse<Object>> handleResponseException(
            ResponseException ex, WebRequest request) {

        String path = request.getDescription(false).replace("uri=", "");
        int statusCode = ex.getResponse().getStatusCode();
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("type", "ResponseException");
        errorDetails.put("elasticsearch_status", statusCode);
        errorDetails.put("reason", ex.getResponse().getWarnings());

        String message = getElasticsearchErrorMessage(statusCode);

        // Add specific details for common Elasticsearch HTTP status codes
        switch (statusCode) {
            case 400:
                errorDetails.put("suggestion", "Check your query syntax and parameters");
                break;
            case 404:
                errorDetails.put("suggestion", "Verify index name and document ID");
                break;
            case 409:
                errorDetails.put("suggestion", "Check document version or index settings");
                break;
            case 429:
                errorDetails.put("suggestion", "Reduce request rate or increase cluster capacity");
                break;
            case 503:
                errorDetails.put("suggestion", "Check Elasticsearch cluster health");
                break;
        }

        MetaResponse<Object> response = MetaResponse.error(
                message,
                statusCode,
                path,
                errorDetails
        );

        return new ResponseEntity<>(response, httpStatus);
    }

    /**
     * Handle connection-related exceptions
     */
    @ExceptionHandler({
            java.net.ConnectException.class,
            java.net.SocketTimeoutException.class,
            java.io.IOException.class
    })
    public ResponseEntity<MetaResponse<Object>> handleConnectionException(
            Exception ex, WebRequest request) {

        String path = request.getDescription(false).replace("uri=", "");

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("type", "ConnectionException");
        errorDetails.put("exception_class", ex.getClass().getSimpleName());
        errorDetails.put("original_message", ex.getMessage());
        errorDetails.put("suggestion", "Check Elasticsearch cluster connectivity and network settings");

        MetaResponse<Object> response = MetaResponse.error(
                "Unable to connect to Elasticsearch cluster",
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                path,
                errorDetails
        );

        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Handle custom Elasticsearch business exceptions
     */
    @ExceptionHandler(ElasticsearchBusinessException.class)
    public ResponseEntity<MetaResponse<Object>> handleElasticsearchBusinessException(
            ElasticsearchBusinessException ex, WebRequest request) {

        String path = request.getDescription(false).replace("uri=", "");

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("type", "BusinessException");
        errorDetails.put("validation_error", true);

        if (ex.getCause() != null) {
            errorDetails.put("cause", ex.getCause().getMessage());
        }

        MetaResponse<Object> response = MetaResponse.error(
                ex.getMessage(),
                ex.getStatus().value(),
                path,
                errorDetails
        );

        return new ResponseEntity<>(response, ex.getStatus());
    }

    /**
     * Handle generic exceptions as fallback
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MetaResponse<Object>> handleGenericException(
            Exception ex, WebRequest request) {

        String path = request.getDescription(false).replace("uri=", "");

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("type", "UnexpectedException");
        errorDetails.put("exception_class", ex.getClass().getSimpleName());
        errorDetails.put("original_message", ex.getMessage());

        MetaResponse<Object> response = MetaResponse.error(
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                path,
                errorDetails
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Get user-friendly error message based on Elasticsearch status code
     */
    private String getElasticsearchErrorMessage(int statusCode) {
        return switch (statusCode) {
            case 400 -> "Bad request - Invalid query or parameters";
            case 404 -> "Resource not found - Index or document does not exist";
            case 409 -> "Conflict - Version mismatch or resource already exists";
            case 429 -> "Too many requests - Rate limit exceeded";
            case 503 -> "Service unavailable - Elasticsearch cluster is not ready";
            default -> "Elasticsearch error occurred";
        };
    }
}
