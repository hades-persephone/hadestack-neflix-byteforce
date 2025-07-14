package io.watch.rating.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.*;
import org.checkerframework.checker.units.qual.Temperature;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("ratings")
public class Rating {
    @PrimaryKey
    private UUID id;

    @NotBlank(message = "movie ID is required")
    @Column("movie_id")
    private UUID movieId;

    @NotBlank(message = "User ID is required")
    @Column("user_id")
    private UUID userId;

    @NotNull(message = "Rating value is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Column("rating_value")
    private Integer ratingValue;

    @Min(value = 1, message = "Fraud Score must be at least 1")
    @Max(value = 5, message = "Fraud Score must be at most 5")
    @Column("fraud_score")
    private Double fraudScore;

    @Size(max = 2000, message = "Review text must not exceed 2000 characters")
    @Column("review_text")
    private String reviewText;

    @Size(max = 20, message = "Review text must not exceed 2000 characters")
    @Column("ip_address")
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    private RatingStatus status = RatingStatus.ACTIVE;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column("created_at")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("is_verified")
    private Boolean isVerified;

    @Column("helpful_count")
    private Integer helpfulCount;

    @Column("is_flagged")
    private Boolean isFlagged;

    @Column("version")
    private Long version;
}
