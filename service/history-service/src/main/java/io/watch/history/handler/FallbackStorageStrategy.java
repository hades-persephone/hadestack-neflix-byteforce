package io.watch.history.handler;

import io.watch.history.entity.ActionHistoryByAction;
import io.watch.history.entity.ActionHistoryByUser;
import io.watch.history.entity.WatchProgress;

import java.util.List;

public class FallbackStorageStrategy {
    public void storeActionHistory(ActionHistoryByAction activity) {

    }

    public void storeUserHistory(ActionHistoryByUser userActivity) {
    }

    public void storeWatchProgress(WatchProgress progress) {
    }

    public void storeBatchHistory(List<ActionHistoryByAction> activities) {
    }

    public boolean supportsBulkOperations() {
        return false;
    }

    public int getPriority() {
        return 0;
    }
}
