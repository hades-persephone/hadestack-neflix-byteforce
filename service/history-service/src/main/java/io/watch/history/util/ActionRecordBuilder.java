package io.watch.history.util;

import io.watch.history.dto.ActionRecord;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for building ActionRecord objects
 */
public class ActionRecordBuilder {

    private String entityType;
    private String entityId;
    private String actionType;
    private String userId;
    private Map<String, String> details = new HashMap<>();
    private String sourceIp;
    private String userAgent;
    private Instant actionTimestamp;

    private ActionRecordBuilder() {
        // Private constructor to enforce builder pattern usage
    }

    public static ActionRecordBuilder create() {
        return new ActionRecordBuilder();
    }

    /**
     * Set entity information
     */
    public ActionRecordBuilder entity(String entityType, String entityId) {
        this.entityType = entityType;
        this.entityId = entityId;
        return this;
    }

    /**
     * Set action type
     */
    public ActionRecordBuilder action(String actionType) {
        this.actionType = actionType;
        return this;
    }

    /**
     * Set user ID
     */
    public ActionRecordBuilder user(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Add a detail key-value pair
     */
    public ActionRecordBuilder addDetail(String key, String value) {
        if (key != null && value != null) {
            this.details.put(key, value);
        }
        return this;
    }

    /**
     * Add multiple details from a map
     */
    public ActionRecordBuilder addDetails(Map<String, String> details) {
        if (details != null) {
            this.details.putAll(details);
        }
        return this;
    }

    /**
     * Set source IP
     */
    public ActionRecordBuilder sourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
        return this;
    }

    /**
     * Set user agent
     */
    public ActionRecordBuilder userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * Set action timestamp
     */
    public ActionRecordBuilder timestamp(Instant actionTimestamp) {
        this.actionTimestamp = actionTimestamp;
        return this;
    }

    /**
     * Auto-populate IP and user agent from current request
     */
    public ActionRecordBuilder withRequestInfo() {
        HttpServletRequest request = Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .orElse(null);

        if (request != null) {
            // Get client IP - check for forwarded headers first
            this.sourceIp = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                    .map(header -> header.split(",")[0].trim())
                    .orElseGet(request::getRemoteAddr);

            // Get user agent
            this.userAgent = request.getHeader("User-Agent");
        }

        return this;
    }

    /**
     * Build the final ActionRecord object
     */
    public ActionRecord build() {
        if (entityType == null || entityId == null) {
            throw new IllegalStateException("Entity type and entity ID are required");
        }
        if (actionType == null) {
            throw new IllegalStateException("Action type is required");
        }

        // Set timestamp if not already set
        if (actionTimestamp == null) {
            actionTimestamp = Instant.now();
        }

        return ActionRecord.builder()
                .entityType(entityType)
                .entityId(entityId)
                .actionType(actionType)
                .userId(userId)
                .details(details)
                .sourceIp(sourceIp)
                .userAgent(userAgent)
                .actionTimestamp(actionTimestamp)
                .build();
    }
}