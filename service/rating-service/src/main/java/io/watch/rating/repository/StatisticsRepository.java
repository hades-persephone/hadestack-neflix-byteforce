package io.watch.rating.repository;

import io.micrometer.core.instrument.config.validate.Validated;
import io.watch.rating.entity.RatingStatistics;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StatisticsRepository extends CassandraRepository<RatingStatistics, UUID> {
    Optional<RatingStatistics> findByMovieId(UUID movieId);
}
