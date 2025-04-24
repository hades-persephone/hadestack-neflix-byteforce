package io.watch.recommendation.repository;

import io.watch.recommendation.model.UserBehavior;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBehaviorRepository extends CassandraRepository<UserBehavior, Long> {
    List<UserBehavior> findByUserId(Long userId);
}
