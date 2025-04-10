package io.watch.movie.repository;

import io.watch.movie.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SeasonRepository extends JpaRepository<Season, UUID> {
}
