package io.watch.movie.repository;

import io.watch.movie.entity.Series;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface SeriesRepository extends JpaRepository<Series, UUID> {

    @Query("SELECT s FROM Series s " +
            "LEFT JOIN FETCH s.seasons seas " +
            "LEFT JOIN FETCH seas.episodes e " +
            "LEFT JOIN FETCH s.categories c " +
            "LEFT JOIN FETCH s.actors a " +
            "LEFT JOIN FETCH s.directors d " +
            "LEFT JOIN FETCH s.languages l " +
            "LEFT JOIN FETCH s.subtitles sub " +
            "LEFT JOIN FETCH s.playlists p " +
            "WHERE s.id = :seriesId AND s.deletedAt IS NULL")
    Optional<Series> findSeriesWithDetails(@Param("seriesId") UUID seriesId);

//    public default SeriesFullResponse findByIdFullResponse(BaseNativeQueryExecutor nativeQuery, UUID seriesId) {
//        Map<String, Object> parameters = new HashMap<>();
//        StringBuilder sql = new StringBuilder();
//        sql.append(" SELECT");
//        sql.append("    s.id AS id,                                                 ");
//        sql.append("    s.title AS title,                                           ");
//        sql.append("    s.description AS description,                               ");
//        sql.append("    s.release_date AS releaseDate,                              ");
//        sql.append("    s.rating_score AS ratingScore,                              ");
//        sql.append("    s.imdb_rating AS imdbRating,                                ");
//        sql.append("    s.rotten_tomatoes_score AS rottenTomatoesScore,             ");
//        sql.append("    s.production_company AS productionCompany,                  ");
//        sql.append("    s.trailer_url AS trailerUrl,                                ");
//        sql.append("    s.poster_url AS posterUrl,                                  ");
//        sql.append("    s.thumbnail_url AS thumbnailUrl,                             ");
//        sql.append("    s.age_rating AS ageRating,                                  ");
//        sql.append("    s.country_of_origin AS countryOfOrigin,                     ");
//        sql.append("    s.view_count AS viewCount,                                  ");
//        sql.append("    s.code AS code,                                             ");
//        sql.append("    s.is_available AS isAvailable,                              ");
//        sql.append("    s.created_at AS createdAt,                                  ");
//        sql.append("    s.updated_at AS updatedAt,                                  ");
//        sql.append("                                                        ");
//        sql.append("    seas.id AS id,                                              ");
//        sql.append("    seas.season_number AS seasonNumber,                         ");
//        sql.append("    seas.title AS title,                                        ");
//        sql.append("    seas.description AS description,                            ");
//        sql.append("    seas.release_date AS releaseDate,                           ");
//        sql.append("    seas.poster_url AS posterUrl,                               ");
//        sql.append("    seas.trailer_url AS trailerUrl,                             ");
//        sql.append("    seas.view_count AS viewCount,                               ");
//        sql.append("    seas.is_available AS isAvailable,                           ");
//        sql.append("    seas.created_at AS createdAt,                               ");
//        sql.append("                                                        ");
//        sql.append("    e.id AS id,                                                 ");
//        sql.append("    e.episode_number AS episodeNumber,                          ");
//        sql.append("    e.title AS title,                                           ");
//        sql.append("    e.description AS description,                               ");
//        sql.append("    e.duration AS duration,                                     ");
//        sql.append("    e.release_date AS releaseDate,                              ");
//        sql.append("    e.rating_score AS ratingScore,                              ");
//        sql.append("    e.view_count AS viewCount,                                  ");
//        sql.append("    e.stream_url AS streamUrl,                                  ");
//        sql.append("    e.thumbnail_url AS thumbnailUrl,                            ");
//        sql.append("    e.file_size AS fileSize,                                    ");
//        sql.append("    e.video_quality AS videoQuality,                            ");
//        sql.append("    e.air_date AS airDate,                                      ");
//        sql.append("    e.runtime_seconds AS runtimeSeconds,                        ");
//        sql.append("    e.is_available AS isAvailable,                              ");
//        sql.append("    e.created_at AS createdAt,                                  ");
//        sql.append("                                                        ");
//        sql.append("    c.id AS id,                                                 ");
//        sql.append("    c.name AS name,                                             ");
//        sql.append("    a.id AS id,                                                 ");
//        sql.append("    a.name AS name,                                             ");
//        sql.append("    d.id AS id,                                                 ");
//        sql.append("    d.name AS name,                                             ");
//        sql.append("    l.id AS id,                                                 ");
//        sql.append("    l.name AS name,                                             ");
//        sql.append("    sub.id AS id,                                               ");
//        sql.append("    sub.name AS name,                                           ");
//        sql.append("    p.id AS id,                                                 ");
//        sql.append("    p.name AS name                                              ");
//        sql.append(" FROM series s ");
//        sql.append(" LEFT JOIN seasons seas ON s.id = seas.series_id AND seas.deleted_at IS NULL");
//        sql.append(" LEFT JOIN episodes e ON seas.id = e.season_id AND e.deleted_at IS NULL");
//        sql.append(" LEFT JOIN series_categories sc ON s.id = sc.series_id");
//        sql.append(" LEFT JOIN categories c ON sc.category_id = c.id");
//        sql.append(" LEFT JOIN series_actors sa ON s.id = sa.series_id");
//        sql.append(" LEFT JOIN actors a ON sa.actor_id = a.id");
//        sql.append(" LEFT JOIN series_directors sd ON s.id = sd.series_id");
//        sql.append(" LEFT JOIN directors d ON sd.director_id = d.id");
//        sql.append(" LEFT JOIN series_languages sl ON s.id = sl.series_id");
//        sql.append(" LEFT JOIN languages l ON sl.language_id = l.id");
//        sql.append(" LEFT JOIN series_subtitles ss ON s.id = ss.series_id");
//        sql.append(" LEFT JOIN subtitles sub ON ss.subtitle_id = sub.id");
//        sql.append(" LEFT JOIN playlists_series ps ON s.id = ps.series_id");
//        sql.append(" LEFT JOIN playlists p ON ps.playlist_id = p.id");
//        sql.append(" WHERE s.id = :seriesId AND s.deleted_at IS NULL");
//        sql.append(" ORDER BY seas.season_number, e.episode_number ");
//
//        return nativeQuery.get(sql.toString(), parameters, SeriesFullResponse.class);
//    }

}
