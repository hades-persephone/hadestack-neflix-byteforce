package io.watch.rating.repository;

import io.micrometer.core.instrument.config.validate.Validated;
import io.watch.rating.entity.Rating;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends CassandraRepository<Rating, UUID> {

    @Query("SELECT * FROM ratings WHERE movie_id = ?0 ALLOW FILTERING")
    Slice<Rating> findByMovieId(String movieId, Pageable pageable);

    @Query("SELECT * FROM ratings WHERE user_id = ?0 ALLOW FILTERING")
    Slice<Rating> findByUserId(UUID userId, Pageable pageable);

    @Query("SELECT * FROM ratings WHERE movie_id = ?0 AND user_id = ?1 ALLOW FILTERING")
    Optional<Rating> findByMovieIdAndUserId(String movieId, String userId);

    @Query("SELECT * FROM ratings WHERE movie_id = ?0 AND is_verified = true ALLOW FILTERING")
    List<Rating> findVerifiedRatingsByMovieId(String movieId);

    @Query("SELECT * FROM ratings WHERE is_flagged = true ALLOW FILTERING")
    List<Rating> findFlaggedRatings();

    @Query("SELECT COUNT(*) FROM ratings WHERE movie_id = ?0 ALLOW FILTERING")
    long countByMovieId(UUID movieId);

    @Query("DELETE FROM ratings WHERE user_id = ?0")
    void deleteByUserId(String userId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.movieId = :movieId ALLOW FILTERING")
    Double getAverageRatingByMovieId(@Param("movieId") UUID movieId);
    
    Long countByUserIdAndCreatedAtAfter(UUID userId, LocalDateTime createdAtAfter);

    Slice<Rating> findActiveByMovieId(UUID movieId, Pageable pageable);

    Validated<Double> findAverageRatingByMovieId(UUID movieId);

    Slice<Rating> findActiveByUserId(UUID userId, Pageable unpaged);

    Boolean existsByUserIdAndMovieId(UUID userId, UUID movieId);

    @Query("SELECT * FROM ratings WHERE rating_value = 5 LIMIT ?0")
    Slice<Rating> findTopRatedMovies(Pageable pageable);

    @Query("SELECT * FROM ratings WHERE rating_value = ?1 LIMIT ?0")
    Slice<Rating> findByRatingValue(int ratingValue, Pageable pageable);

}
