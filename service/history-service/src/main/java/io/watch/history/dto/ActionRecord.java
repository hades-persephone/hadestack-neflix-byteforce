package io.watch.history.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionRecord {

    private String entityType;
    private String entityId;
    private Instant actionTimestamp;
    private String actionType;
    private String userId;
    private Map<String, String> details;
    private String sourceIp;
    private String userAgent;

    public String getYearMonth() {
        return String.format("%tY-%tm", actionTimestamp, actionTimestamp);
    }
}