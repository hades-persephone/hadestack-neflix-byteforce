package io.watch.movie.repository;

import io.watch.movie.entity.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WatchHistoryRepository extends JpaRepository<WatchHistory, UUID> {
}
