package io.watch.history.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BatchContainer<T> {
    private long batchId;
    private String timestamp;
    private int size;
    private List<T> actions;
}
