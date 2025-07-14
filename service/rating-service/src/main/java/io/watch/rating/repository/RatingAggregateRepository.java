package io.watch.rating.repository;

import io.watch.rating.entity.RatingAggregate;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingAggregateRepository extends CassandraRepository<RatingAggregate, String> {
}