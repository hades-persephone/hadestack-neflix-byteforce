package io.watch.rating.entity;

import lombok.*;
import org.springframework.data.annotation.Version;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("rating_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingStatistics {
    @PrimaryKey
    private UUID movieId;

    private Double averageRating;
    private Long totalRatings;
    private Long oneStarCount;
    private Long twoStarCount;
    private Long threeStarCount;
    private Long fourStarCount;
    private Long fiveStarCount;

    @Version
    private Long version;

    private LocalDateTime lastUpdated;
}