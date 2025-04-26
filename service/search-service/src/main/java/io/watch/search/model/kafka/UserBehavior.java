package io.watch.search.model.kafka;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserBehavior implements Serializable {
    private Long userId;
    private Long profileId;
    private Long movieId;
    private String eventType;
    private Long timestamp;
}