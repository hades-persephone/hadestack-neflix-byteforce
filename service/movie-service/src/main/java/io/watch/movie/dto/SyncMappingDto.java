package io.watch.movie.dto;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SyncMappingDto {
    private String sourceTable;
    private String targetTable;
    private String sourceIdColumn;
    private String targetIdColumn;
    private String transformationScript;
    private Map<String, String> columnMappings;
    private boolean active ;
}
