package io.watch.history.dto;

import io.watch.history.entity.ActionHistoryByAction;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FailedActivityRecord {
    private ActionHistoryByAction activity;
    private String exception;
    private Instant timestamp;
    private int retryCount;
}
