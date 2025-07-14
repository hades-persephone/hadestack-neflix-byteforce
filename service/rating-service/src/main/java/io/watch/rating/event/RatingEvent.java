package io.watch.rating.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RatingCreatedEvent.class, name = "RATING_CREATED"),
        @JsonSubTypes.Type(value = RatingUpdatedEvent.class, name = "RATING_UPDATED"),
        @JsonSubTypes.Type(value = RatingDeletedEvent.class, name = "RATING_DELETED")
})
@Getter
@Setter
public abstract class RatingEvent {
    private UUID eventId;
    private UUID aggregateId;
    private String eventType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
