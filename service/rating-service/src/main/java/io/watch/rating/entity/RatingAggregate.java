package io.watch.rating.entity;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;

import java.time.LocalDateTime;
import java.util.Map;

public class RatingAggregate {
    @PrimaryKey
    @Column("movie_id")
    private String movieId;

    @Column("average_rating")
    private Double averageRating;

    @Column("total_ratings")
    private Long totalRatings;

    @Column("rating_distribution")
    private Map<Integer, Long> ratingDistribution;

    @Column("last_updated")
    private LocalDateTime lastUpdated;
}
