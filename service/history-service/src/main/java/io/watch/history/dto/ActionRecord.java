package io.watch.history.dto;


import io.watch.history.entity.UserLoginKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionRecord {
    private UserLoginKey key;
    private String entityType;
    private String entityId;
    private Instant actionTimestamp;
    private String actionType;
    private UUID userId;
    private Map<String, String> details;
    private String sourceIp;
    private String userAgent;
    private String deviceType;
    private String country;

    public String getYearMonth() {
        Date date = Date.from(actionTimestamp);
        return String.format("%tY-%tm", date, date);
    }
}