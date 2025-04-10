package io.watch.movie.repository;

import io.watch.movie.entity.Subtitle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubtitleRepository extends JpaRepository<Subtitle, UUID> {
}
