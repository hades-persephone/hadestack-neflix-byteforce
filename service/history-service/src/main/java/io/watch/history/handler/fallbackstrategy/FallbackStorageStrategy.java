package io.watch.history.handler.fallbackstrategy;

import io.watch.history.entity.ActionHistoryByAction;
import io.watch.history.entity.ActionHistoryByUser;
import io.watch.history.entity.WatchProgress;

import java.util.List;

public interface  FallbackStorageStrategy {
    void storeActionHistory(ActionHistoryByAction activity);
    void storeUserHistory(ActionHistoryByUser userActivity);
    void storeWatchProgress(WatchProgress progress);
    void storeBatchHistory(List<ActionHistoryByAction> activities);
    boolean supportsBulkOperations();
    int getPriority();
    String getStrategyName();
}
