package io.watch.history.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BatchMetadata {
    private Long batchId;
    private int size;
    private List<String> userIds;
    private long timestamp;
}
