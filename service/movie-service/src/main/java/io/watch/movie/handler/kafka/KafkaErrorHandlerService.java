package io.watch.movie.handler.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.watch.movie.entity.FailedMessage;
import io.watch.movie.handler.DlqMessage;
import io.watch.movie.handler.ErrorContext;
import io.watch.movie.repository.FailedMessageRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.*;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaErrorHandlerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaMetricsService metricsService;
    private final FailedMessageRepository failedMessageRepository;

    private static final String DLQ_SUFFIX = "-dlq";
    private static final String RETRY_SUFFIX = "-retry";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final List<String> RETRYABLE_ERRORS = Arrays.asList(
            "NetworkException", "TimeoutException", "DisconnectedException",
            "NotLeaderForPartitionException", "LeaderNotAvailableException"
    );
    private final ContentNegotiatingViewResolver contentNegotiatingViewResolver;

    public void handlerKafkaError(String originalTopic, String key, String message, Throwable error, Map<String, Object> headers) {
        ErrorContext errorContext = createErrorContext(originalTopic, key, message, error, headers);

        log.warn("Kafka error occurred: topic={}, key={}, message={}, error={}", originalTopic, key, message, error.getMessage());

        ErrorType errorType = classifyError(error);
        ErrorAction action = determineAction(errorType, errorContext);

        metricsService.recordError(errorType, originalTopic);
        executeAction(action, errorContext);
    }

    private void executeAction(ErrorAction action, ErrorContext errorContext) {
        switch (action) {
            case RETRY:
                scheduleRetry(errorContext);
                break;
            case SEND_TO_DLQ:
                sendToDeadLetterQueue(errorContext);
                break;
            case COMPRESS_AND_RETRY:
                compressAndRetry(errorContext);
                break;
            case REFORMAT_AND_RETRY:
                reformatAndRetry(errorContext);
                break;
            case VALIDATE_AND_RETRY:
                validateAndRetry(errorContext);
                break;
            case SKIP_AND_LOG:
                skipAndLog(errorContext);
            default:
                log.warn("Unknown action: {}", action);
                sendToDeadLetterQueue(errorContext);
        }
    }

    private void skipAndLog(ErrorContext errorContext) {
        log.warn("Skipping message due to error: topic={}, key={}, error={}",
                errorContext.getOriginalTopic(), errorContext.getKey(), errorContext.getError().getMessage());

        metricsService.recordSkippedMessage(errorContext.getOriginalTopic());

        // Store for audit purposes
        storeSkippedMessage(errorContext);
    }

    private void storeSkippedMessage(ErrorContext errorContext) {
        try {
            FailedMessage failedMessage = FailedMessage.builder()
                    .topic(errorContext.getOriginalTopic())
                    .key(errorContext.getKey())
                    .message(errorContext.getMessage())
                    .errorType(classifyError(errorContext.getError()).name())
                    .errorMessage(errorContext.getError().getMessage())
                    .retryCount(errorContext.getRetryCount())
                    .createdAt(LocalDateTime.now())
                    .status(FailedMessageStatus.FAILED)
                    .build();

            failedMessageRepository.save(failedMessage);
            log.info("Stored failed message in database: id={}", failedMessage.getId());

        } catch (DataAccessException e) {
            log.error("❌ Failed to store failed message in database", e);
            // Send alert to monitoring system
//            notificationService.sendCriticalAlert("Failed to store failed Kafka message", e);
        }
    }

    private void validateAndRetry(ErrorContext errorContext) {
        try {
            if(isMessageValid(errorContext.getMessage())) {
                scheduleRetry(errorContext);
            } else {
                log.warn("Message validation failed, sending to DLQ");
                sendToDeadLetterQueue(errorContext);
            }
        } catch (Exception e) {
            log.error("Message validation failed", e);
            sendToDeadLetterQueue(errorContext);
        }
    }

    private boolean isMessageValid(String message) {
        return true;
    }

    public Map<String, Object> getErrorsStatistics() {
        return metricsService.getErrorStatistics();
    }

    private void reformatAndRetry(ErrorContext errorContext) {
        try {
            String reformatedMessage = reformatMessage(errorContext.getMessage());
            ErrorContext newContext = errorContext.withMessage(reformatedMessage);
            scheduleRetry(newContext);
        } catch (Exception e) {
            log.error("Message reformating failed", e);
            sendToDeadLetterQueue(errorContext);
        }
    }

    private String reformatMessage(String message) {
        return message;
    }

    private void compressAndRetry(ErrorContext errorContext) {
        try {
            String compressedMessage = compressMessage(errorContext.getMessage());

            if(errorContext.getMessage().length() > compressedMessage.length()) {
                log.info("Compressed message from {} to {} bytes", errorContext.getMessage().length(), compressedMessage.length());

                ErrorContext newContext = errorContext.withMessage(compressedMessage);
                scheduleRetry(newContext);
            } else {
                log.warn("Compression did not reduce message size, sending to DLQ");
                sendToDeadLetterQueue(errorContext);
            }
        } catch (Exception e) {
            log.warn("Message compression failed: {}", e.getMessage());
            sendToDeadLetterQueue(errorContext);
        }
    }

    private String compressMessage(String message) {
        return message;
    }

    private void sendToDeadLetterQueue(ErrorContext context) {
        String dlq = context.getOriginalTopic() + DLQ_SUFFIX;

        try {
            DlqMessage dlqMessage = DlqMessage.builder()
                    .originalTopic(context.getOriginalTopic())
                    .originalKey(context.getKey())
                    .originalMessage(context.getMessage())
                    .errorType(classifyError(context.getError()).name())
                    .errorMessage(context.getError().getMessage())
                    .retryCount(context.getRetryCount())
                    .timestamp(LocalDateTime.now())
                    .headers(context.getHeaders())
                    .build();

            String dlqJson = objectMapper.writeValueAsString(dlqMessage);
            kafkaTemplate.send(dlq, context.getKey(), dlqJson)
                    .whenComplete((result, ex) -> {
                        if(ex != null) {
                            log.error("Failed to send to DLQ: {}, error: {}", dlq, ex.getMessage());
                            storeFailedMessage(context);
                        } else {
                            log.info("Send to DLQ: {}, key: {}", dlq, context.getKey());
                            metricsService.recordDlqSend(dlq);
                        }
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void retryFailedMessages(List<UUID> messageIds) {
        List<FailedMessage> failedMessages = failedMessageRepository.findAllById(messageIds);

        for (FailedMessage msg : failedMessages) {
            ErrorContext context = ErrorContext.builder()
                    .originalTopic(msg.getTopic())
                    .key(msg.getKey())
                    .message(msg.getMessage())
                    .retryCount(msg.getRetryCount())
                    .build();

            scheduleRetry(context);
        }
    }

    private void storeFailedMessage(ErrorContext context) {
        try {
            FailedMessage failedMessage = FailedMessage.builder()
                    .topic(context.getOriginalTopic())
                    .key(context.getKey())
                    .message(context.getMessage())
                    .errorType(classifyError(context.getError()).name())
                    .errorMessage(context.getError().getMessage())
                    .retryCount(context.getRetryCount())
                    .createdAt(LocalDateTime.now())
                    .status(FailedMessageStatus.FAILED)
                    .build();

            failedMessageRepository.save(failedMessage);
            log.info("💾 Stored failed message in database: id={}", failedMessage.getId());

        } catch (DataAccessException e) {
            log.error("❌ Failed to store failed message in database", e);
            // Send alert to monitoring system
            // notificationService.sendCriticalAlert("Failed to store failed Kafka message", e);
        }
    }

    @Async
    protected void scheduleRetry(ErrorContext errorContext) {
        int retryCount = errorContext.getRetryCount() - 1;
        long delay = calculateBackoffDelay(retryCount);

        log.info("🔄 Scheduling retry #{} for topic: {} after {}ms", retryCount, errorContext.getOriginalTopic(), delay);

        try {
            Thread.sleep(delay);

            Map<String, Object> updateHeaders = new HashMap<>(errorContext.getHeaders());
            updateHeaders.put("retry-count", retryCount);
            updateHeaders.put("original-error", errorContext.getError().getMessage());

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(errorContext.getOriginalTopic(), errorContext.getKey(), errorContext.getMessage());

            future.whenComplete((result, ex) -> {
                if(ex != null) {
                    ErrorContext newContext = errorContext.withRetryCount(retryCount);
                    handlerKafkaError(errorContext.getOriginalTopic(), errorContext.getKey(), errorContext.getMessage(), ex, updateHeaders);
                } else {
                    log.info("Retry successful for topic: {}, key: {}", errorContext.getOriginalTopic(), errorContext.getKey());
                    metricsService.recordRetrySuccess(errorContext.getOriginalTopic());
                }
            });

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private long calculateBackoffDelay(int retryCount) {
        return Math.min(1000 * (long) Math.pow(2, retryCount - 1), 30000);
    }

    private ErrorAction determineAction(ErrorType errorType, ErrorContext errorContext) {
        return switch (errorType) {
            case TIMEOUT, NETWORK, LEADER_ELECTION, RETRIABLE ->
                    errorContext.getRetryCount() < MAX_RETRY_ATTEMPTS ? ErrorAction.RETRY : ErrorAction.SEND_TO_DLQ;
            case MESSAGE_TOO_LARGE -> ErrorAction.COMPRESS_AND_RETRY;
            case SERIALIZATION -> ErrorAction.REFORMAT_AND_RETRY;
            case POLICY_VIOLATION -> ErrorAction.VALIDATE_AND_RETRY;
            case OUT_OF_ORDER, DUPLICATE_SEQUENCE -> ErrorAction.SKIP_AND_LOG;
            default -> ErrorAction.SEND_TO_DLQ;
        };
    }

    private ErrorType classifyError(Throwable error) {
        if(error instanceof TimeoutException) {
            return ErrorType.TIMEOUT;
        } else if(error instanceof RecordTooLargeException) {
            return ErrorType.MESSAGE_TOO_LARGE;
        } else if (error instanceof TopicAuthorizationException) {
            return ErrorType.AUTHORIZATION;
        } else if (error instanceof NetworkException || error instanceof DisconnectException) {
            return ErrorType.NETWORK;
        } else if (error instanceof NotLeaderOrFollowerException || error instanceof LeaderNotAvailableException) {
            return ErrorType.LEADER_ELECTION;
        } else if (error instanceof SerializationException) {
            return ErrorType.SERIALIZATION;
        } else if (error instanceof InvalidTopicException) {
            return ErrorType.INVALID_TOPIC;
        } else if (error instanceof PolicyViolationException) {
            return ErrorType.POLICY_VIOLATION;
        } else if (error instanceof OutOfOrderSequenceException) {
            return ErrorType.OUT_OF_ORDER;
        } else if (error instanceof DuplicateSequenceException) {
            return ErrorType.DUPLICATE_SEQUENCE;
        } else if (error instanceof UnknownTopicOrPartitionException) {
            return ErrorType.UNKNOWN_TOPIC_PARTITION;
        } else if (error instanceof RetriableException) {
            return ErrorType.RETRIABLE;
        } else {
            return ErrorType.UNKNOWN;
        }
    }

    private ErrorContext createErrorContext(String originalTopic, String key, String message, Throwable error, Map<String, Object> headers) {
        return ErrorContext.builder()
                .originalTopic(originalTopic)
                .key(key)
                .message(message)
                .error(error)
                .headers(headers != null ? headers : new HashMap<>())
                .retryCount(0)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public enum ErrorType {
        TIMEOUT, MESSAGE_TOO_LARGE, AUTHORIZATION, NETWORK, LEADER_ELECTION,
        SERIALIZATION, INVALID_TOPIC, POLICY_VIOLATION, OUT_OF_ORDER,
        DUPLICATE_SEQUENCE, UNKNOWN_TOPIC_PARTITION, RETRIABLE, UNKNOWN
    }

    public enum ErrorAction {
        RETRY, SEND_TO_DLQ, COMPRESS_AND_RETRY, REFORMAT_AND_RETRY, VALIDATE_AND_RETRY, SKIP_AND_LOG
    }

    public enum FailedMessageStatus {
        FAILED, SKIPPED, RETRYING, RESOLVED
    }

}
