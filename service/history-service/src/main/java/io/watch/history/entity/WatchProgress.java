package io.watch.history.entity;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Table("watch_progress")
public class WatchProgress {

    @PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    @NotNull(message = "userId cannot be null")
    @Size(min = 1, max = 36, message = "userId must be between 1 and 36 characters")
    private UUID userId; // Unique identifier for the user

    @PrimaryKeyColumn(name = "content_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    @NotNull(message = "contentId cannot be null")
    @Size(min = 1, max = 50, message = "contentId must be between 1 and 50 characters")
    @Pattern(regexp = "^(movie|series):[a-zA-Z0-9:]+$", message = "contentId must follow format 'movie:<id>' or 'series:<id>:season<no>:ep<no>'")
    private String contentId; // Movie or episode ID (e.g., "movie:123", "series:456:season1:ep1")

    @PrimaryKeyColumn(name = "progress_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    @NotNull(message = "progressId cannot be null")
    private UUID progressId; // Unique ID for progress record

    @NotNull(message = "timestamp cannot be null")
    private Instant timestamp; // When the progress was last updated

    @Min(value = 0, message = "positionMillis must be non-negative")
    private Long positionMillis; // Position in milliseconds (e.g., 360000 for 6 minutes)

    @Min(value = 0, message = "percentage must be between 0 and 1")
    @Max(value = 1, message = "percentage must be between 0 and 1")
    private Double percentage; // Percentage of content watched (0.0 to 1.0)

    @NotBlank(message = "deviceType cannot be blank")
    @Size(max = 20, message = "deviceType must not exceed 20 characters")
    @Pattern(regexp = "^(mobile|tv|web|tablet|console)$", message = "deviceType must be one of: mobile, tv, web, tablet, console")
    private String deviceType; // Device used (e.g., "mobile", "tv", "web")

    @Size(max = 45, message = "sourceIp must not exceed 45 characters")
    private String sourceIp; // IP address of the device (IPv4/IPv6)

    @Size(max = 255, message = "userAgent must not exceed 255 characters")
    private String userAgent; // User agent of the device

    private Instant sessionStart; // Start time of the viewing session

    private Boolean completed; // Whether the content was fully watched

    @Size(max = 36, message = "profileId must not exceed 36 characters")
    private String profileId; // Sub-profile ID for multi-profile accounts (e.g., "kid", "main")

    @Min(value = 0, message = "durationMillis must be non-negative")
    private Integer durationMillis; // Total duration of the content

    @NotBlank(message = "contentType cannot be blank")
    @Pattern(regexp = "^(movie|episode)$", message = "contentType must be 'movie' or 'episode'")
    private String contentType; // "movie" or "episode"

    @Size(max = 10, message = "metadata cannot have more than 10 entries")
    private Map<String, String> metadata;
}
