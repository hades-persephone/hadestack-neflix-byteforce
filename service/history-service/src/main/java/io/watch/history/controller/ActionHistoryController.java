package io.watch.history.controller;

import io.watch.history.dto.ActionRecord;
import io.watch.history.service.ActionHistoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ActionHistoryController {

    private final ActionHistoryService actionHistoryService;

    /**
     * Record a single action
     */
    @PostMapping("/record")
    public ResponseEntity<Void> recordAction(@RequestBody @Valid ActionRecord actionRecord) {
        actionHistoryService.recordAction(actionRecord);
        return ResponseEntity.accepted().build();
    }

    /**
     * Record multiple actions in batch
     */
    @PostMapping("/record/batch")
    public ResponseEntity<Void> recordActions(@RequestBody @Valid List<ActionRecord> actionRecords) {
        actionHistoryService.recordActions(actionRecords);
        return ResponseEntity.accepted().build();
    }

    /**
     * Get action history for an entity
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<ActionRecord>> getEntityHistory(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @RequestParam(defaultValue = "50") @Min(1) @Max(1000) int limit) {

        List<ActionRecord> history = actionHistoryService.getActionHistoryForEntity(
                entityType, entityId, limit);

        return ResponseEntity.ok(history);
    }

    /**
     * Get action history for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ActionRecord>> getUserHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "50") @Min(1) @Max(1000) int limit) {

        List<ActionRecord> history = actionHistoryService.getActionHistoryForUser(userId, limit);
        return ResponseEntity.ok(history);
    }

    /**
     * Get action history for an action type
     */
    @GetMapping("/action/{actionType}")
    public ResponseEntity<List<ActionRecord>> getActionTypeHistory(
            @PathVariable String actionType,
            @RequestParam(defaultValue = "50") @Min(1) @Max(1000) int limit) {

        List<ActionRecord> history = actionHistoryService.getActionHistoryByType(actionType, limit);
        return ResponseEntity.ok(history);
    }

    /**
     * Get action history for a time period
     */
    @GetMapping("/time/{yearMonth}")
    public ResponseEntity<List<ActionRecord>> getTimeHistory(
            @PathVariable String yearMonth,
            @RequestParam(defaultValue = "50") @Min(1) @Max(1000) int limit) {

        List<ActionRecord> history = actionHistoryService.getActionHistoryByTime(yearMonth, limit);
        return ResponseEntity.ok(history);
    }

    /**
     * Get action counts for entity type and action type
     */
    @GetMapping("/stats/{entityType}/{actionType}")
    public ResponseEntity<Map<String, Long>> getActionCounts(
            @PathVariable String entityType,
            @PathVariable String actionType) {

        Map<String, Long> counts = actionHistoryService.getActionCounts(entityType, actionType);
        return ResponseEntity.ok(counts);
    }
}