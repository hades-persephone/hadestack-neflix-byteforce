package io.watch.history.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
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
    private String deviceType;
    private String country;

    public String getYearMonth() {
        Date date = Date.from(actionTimestamp);
        return String.format("%tY-%tm", date, date);
    }
}