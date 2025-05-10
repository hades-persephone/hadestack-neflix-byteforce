package io.watch.movie.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class DatabaseChangeEvent implements Serializable {
    private String operation;
    private String tableName;
    private JsonNode before;
    private JsonNode after;
    private UUID entityId;
    private String timestamp;
}
