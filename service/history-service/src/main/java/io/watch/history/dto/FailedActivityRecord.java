package io.watch.history.dto;

import io.watch.history.entity.ActionHistoryByAction;
import io.watch.history.entity.ActionHistoryByUser;
import io.watch.history.entity.WatchProgress;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FailedActivityRecord {
    private ActionHistoryByAction activity;
    private ActionHistoryByUser user;
    private String exception;
    private Instant timestamp;
    private int retryCount;
    private String operationId;
    private WatchProgress progress;
}
