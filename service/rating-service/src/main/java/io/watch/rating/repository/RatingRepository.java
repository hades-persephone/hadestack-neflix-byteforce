package io.watch.rating.repository;

import io.micrometer.core.instrument.config.validate.Validated;
import io.watch.rating.entity.Rating;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends CassandraRepository<Rating, UUID> {

    @Query("SELECT * FROM ratings WHERE product_id = ?0 ALLOW FILTERING")
    Slice<Rating> findByProductId(String productId, Pageable pageable);

    @Query("SELECT * FROM ratings WHERE user_id = ?0 ALLOW FILTERING")
    Slice<Rating> findByUserId(String userId, Pageable pageable);

    @Query("SELECT * FROM ratings WHERE product_id = ?0 AND user_id = ?1 ALLOW FILTERING")
    Optional<Rating> findByProductIdAndUserId(String productId, String userId);

    @Query("SELECT * FROM ratings WHERE product_id = ?0 AND is_verified = true ALLOW FILTERING")
    List<Rating> findVerifiedRatingsByProductId(String productId);

    @Query("SELECT * FROM ratings WHERE is_flagged = true ALLOW FILTERING")
    List<Rating> findFlaggedRatings();

    @Query("SELECT COUNT(*) FROM ratings WHERE product_id = ?0 ALLOW FILTERING")
    long countByProductId(String productId);

    @Query("DELETE FROM ratings WHERE user_id = ?0")
    void deleteByUserId(String userId);

    Long countByUserIdAndCreatedAtAfter(UUID userId, LocalDateTime createdAtAfter);

    Slice<Rating> findActiveByMovieId(UUID movieId, Pageable pageable);

    Validated<Double> findAverageRatingByMovieId(UUID movieId);

    Slice<Rating> findActiveByUserId(UUID userId, Pageable unpaged);
}
