package io.watch.movie.repository;

import io.watch.movie.entity.Series;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SeriesRepository extends JpaRepository<Series, UUID> {
}
