package io.watch.sync.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Entity
@Data
@Getter
@Setter
@Table(name = "sync_mapping")
public class SyncMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sourceTable;

    @Column(nullable = false)
    private String targetTable;

    @Column(nullable = false)
    private String sourceIdColumn;

    @Column(nullable = false)
    private String targetIdColumn;

    @Column
    private String transformationScript;

    @ElementCollection
    @CollectionTable(name = "column_mappings", joinColumns = @JoinColumn(name = "mapping_id"))
    @MapKeyColumn(name = "source_column")
    @Column(name = "target_column")
    private Map<String, String> columnMappings = new HashMap<>();

    @Column(nullable = false)
    private boolean active = true;
}