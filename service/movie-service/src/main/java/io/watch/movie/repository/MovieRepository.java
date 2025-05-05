package io.watch.movie.repository;

import io.watch.basedata.data.common.BaseNativeQueryExecutor;
import io.watch.basedata.data.query.SearchParams;
import io.watch.basedata.dto.DataResults;
import io.watch.basedata.util.CommonUtil;
import io.watch.movie.dto.request.MovieRequestSearch;
import io.watch.movie.dto.response.ContentResponse;
import io.watch.movie.dto.response.MovieResponse;
import io.watch.movie.entity.Movie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@EnableRedisRepositories
@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {
    Page<Movie> findAll(Pageable pageable);
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    List<Movie> findByCategoriesId(UUID categoryId);

    @Query("SELECT m FROM Movie m WHERE m.isAvailable = true AND m.id = ?1")
    Optional<Movie> findByIdAndIsAvailableTrue(UUID uuid);

    public default DataResults<MovieResponse> search(BaseNativeQueryExecutor query, MovieRequestSearch request, Pageable pageable, HttpServletRequest req) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT                                  ");
        sql.append("         mo.id AS id,");
        sql.append("         mo.title AS title,");
        sql.append("         mo.description AS description,");
        sql.append("         mo.duration AS duration,");
        sql.append("         mo.release_date AS release_date,");
        sql.append("         mo.rating_score AS rating_score,");
        sql.append("         mo.imdb_rating AS imdb_rating,");
        sql.append("         mo.rotten_tomatoes_score AS rotten_tomatoes_score,");
        sql.append("         mo.production_company AS production_company,");
        sql.append("         mo.budget AS budget,");
        sql.append("         mo.box_office AS box_office,");
        sql.append("         mo.trailer_url AS trailer_url,");
        sql.append("         mo.poster_url AS poster_url,");
        sql.append("         mo.thumbnail_url AS thumbnail_url,");
        sql.append("         mo.video_quality AS video_quality,");
        sql.append("         mo.age_rating AS age_rating,");
        sql.append("         mo.country_of_origin AS country_of_origin,");
        sql.append("         mo.view_count AS view_count,");
        sql.append("         mo.stream_url AS stream_url,");
        sql.append("         mo.file_size AS file_size,");
        sql.append("         mo.runtime_seconds AS runtime_seconds,");
        sql.append("        STRING_AGG(DISTINCT c.name, ',') AS category_names,");
        sql.append("        STRING_AGG(DISTINCT a.full_name, ',') AS actor_names,");
        sql.append("        STRING_AGG(DISTINCT d.full_name, ',') AS director_names,");
        sql.append("        STRING_AGG(DISTINCT l.name, ',') AS language_names");
        sql.append(" FROM movies mo                          ");
        sql.append(" LEFT JOIN movies_categories mc ON mo.id = mc.movie_id          ");
        sql.append(" LEFT JOIN categories c ON mc.category_id = c.id and c.is_available = TRUE           ");
        sql.append(" LEFT JOIN movies_actors ma ON mo.id = ma.movie_id                                  ");
        sql.append(" LEFT JOIN actors a ON ma.actor_id = a.id and a.is_available = TRUE           ");
        sql.append(" LEFT JOIN movies_directors md ON mo.id = md.movie_id           ");
        sql.append(" LEFT JOIN directors d ON md.director_id = d.id and d.is_available = TRUE        ");
        sql.append(" LEFT JOIN movies_languages ml ON mo.id = ml.movie_id           ");
        sql.append(" LEFT JOIN languages l ON ml.language_id = l.id and l.is_available = TRUE        ");
        sql.append(" WHERE 1=1 AND mo.is_available = TRUE AND mo.deleted_at IS NULL ");

        if(!CommonUtil.isEmpty(request)) {
            if(!CommonUtil.isNullOrEmpty(request.getTitle())) {
                sql.append(" AND (mo.title LIKE CONCAT('%', :title, '%') OR mo.description LIKE CONCAT('%', :title, '%') ) ");
                params.put("title", request.getTitle());
            }

            if(!CommonUtil.isNullOrEmpty(request.getVideoQuality())) {
                sql.append(" AND mo.video_quality = :videoQuality ");
                params.put("videoQuality", request.getVideoQuality());
            }

            // Handle 'language'
            if (!CommonUtil.isNullOrEmpty(request.getLanguage())) {
                sql.append(" AND l.name IN :languages ");
                params.put("languages", request.getLanguage());
            }

            // Handle 'country'
            if (!CommonUtil.isNullOrEmpty(request.getCountry())) {
                sql.append(" AND mo.country_of_origin IN :countries ");
                params.put("countries", request.getCountry());
            }

            // Handle 'genre'
            if (!CommonUtil.isNullOrEmpty(request.getGenre())) {
                sql.append(" AND c.name IN :genres ");
                params.put("genres", request.getGenre());
            }

            // Handle 'director'
            if (!CommonUtil.isNullOrEmpty(request.getDirector())) {
                sql.append(" AND d.full_name IN :directors ");
                params.put("directors", request.getDirector());
            }

            // Handle 'actor'
            if (!CommonUtil.isNullOrEmpty(request.getActor())) {
                sql.append(" AND a.full_name IN :actors ");
                params.put("actors", request.getActor());
            }

            if(!CommonUtil.isNullOrEmpty(request.getAgeRating())) {
                sql.append(" AND mo.age_rating = :ageRating ");
                params.put("ageRating", request.getAgeRating());
            }

            // Handle 'yearStart' and 'yearEnd'
            if (!CommonUtil.isNullOrEmpty(request.getYearStart()) && !CommonUtil.isNullOrEmpty(request.getYearEnd())) {
                sql.append(" AND YEAR(mo.release_date) BETWEEN :yearStart AND :yearEnd ");
                params.put("yearStart", Integer.parseInt(request.getYearStart()));
                params.put("yearEnd", Integer.parseInt(request.getYearEnd()));
            } else {
                if (!CommonUtil.isNullOrEmpty(request.getYearStart())) {
                    sql.append(" AND YEAR(mo.release_date) >= :yearStart ");
                    params.put("yearStart", Integer.parseInt(request.getYearStart()));
                }
                if (!CommonUtil.isNullOrEmpty(request.getYearEnd())) {
                    sql.append(" AND YEAR(mo.release_date) <= :yearEnd ");
                    params.put("yearEnd", Integer.parseInt(request.getYearEnd()));
                }
            }

            // Handle 'imdbRatingRangeMin' and 'imdbRatingRangeMax'
            if (request.getImdbRatingRangeMin() != null && request.getImdbRatingRangeMax() != null) {
                sql.append(" AND mo.imdb_rating BETWEEN :imdbRatingRangeMin AND :imdbRatingRangeMax ");
                params.put("imdbRatingRangeMin", request.getImdbRatingRangeMin());
                params.put("imdbRatingRangeMax", request.getImdbRatingRangeMax());
            }

            // Handle 'ratingScoreRangeMin' and 'ratingScoreRangeMax'
            if (request.getRatingScoreRangeMin() != null && request.getRatingScoreRangeMax() != null) {
                sql.append(" AND mo.rating_score BETWEEN :ratingScoreRangeMin AND :ratingScoreRangeMax ");
                params.put("ratingScoreRangeMin", request.getRatingScoreRangeMin());
                params.put("ratingScoreRangeMax", request.getRatingScoreRangeMax());
            }

            // Handle 'releaseStartDate' and 'releaseEndDate'
            if(!CommonUtil.isNullOrEmpty(request.getReleaseStartDate()) && !CommonUtil.isNullOrEmpty(request.getReleaseEndDate())) {
                sql.append(" AND mo.release_date BETWEEN :startDate AND :endDate ");
                params.put("startDate", CommonUtil.parseDate(request.getReleaseStartDate()));
                params.put("endDate", CommonUtil.parseDate(request.getReleaseEndDate()));
            } else {
                if(!CommonUtil.isNullOrEmpty(request.getReleaseStartDate())) {
                    sql.append(" AND mo.release_date >= :startDate ");
                    params.put("startDate", CommonUtil.parseDate(request.getReleaseStartDate()));
                }
                if(!CommonUtil.isNullOrEmpty(request.getReleaseEndDate())) {
                    sql.append(" AND mo.release_date <= :endDate ");
                    params.put("endDate", CommonUtil.parseDate(request.getReleaseEndDate()));
                }
            }
        }
        sql.append(" GROUP BY mo.id, mo.title, mo.description, mo.duration, mo.release_date, ");
        sql.append(" mo.rating_score, mo.imdb_rating, mo.rotten_tomatoes_score, ");
        sql.append(" mo.production_company, mo.budget, mo.box_office, mo.trailer_url, ");
        sql.append(" mo.poster_url, mo.thumbnail_url, mo.video_quality, mo.age_rating, ");
        sql.append(" mo.country_of_origin, mo.view_count, mo.stream_url, mo.file_size, ");
        sql.append(" mo.runtime_seconds ");
        String order = " ORDER BY mo.release_date DESC, mo.id ASC ";
        return query.findPagination(sql.toString(), order, params, MovieResponse.class, pageable.getPageSize(), req);
    }

    public default DataResults<MovieResponse> seriesView(BaseNativeQueryExecutor query, MovieRequestSearch request, Pageable pageable, HttpServletRequest req) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder sql = new StringBuilder();
        sql.append(" WITH SeriesViews AS (");
        sql.append("         SELECT");
        sql.append("         s.id,");
        sql.append("         s.title,");
        sql.append("         s.description,");
        sql.append("         s.release_date,");
        sql.append("         s.rating_score,");
        sql.append("         s.view_count,");
        sql.append("         s.poster_url,");
        sql.append("         s.is_available,");
        sql.append("         s.code,");
        sql.append("         SUM(e.view_count) AS total_episode_views");
        sql.append("         FROM series s");
        sql.append("         LEFT JOIN seasons seas ON s.id = seas.series_id");
        sql.append("         LEFT JOIN episodes e ON seas.id = e.season_id");
        sql.append("         WHERE s.is_available = TRUE");
        sql.append("         AND s.deleted_at IS NULL");
        sql.append("         GROUP BY s.id, s.title, s.description, s.release_date, s.rating_score, s.view_count, s.poster_url, s.is_available, s.code");
        sql.append(" )");
        sql.append(" SELECT");
        sql.append("         id,");
        sql.append("         title,");
        sql.append("         description,");
        sql.append("         release_date,");
        sql.append("         rating_score,");
        sql.append("         view_count,");
        sql.append("         poster_url,");
        sql.append("         is_available,");
        sql.append("         code,");
        sql.append("         total_episode_views,");
        sql.append(" AVG(total_episode_views) OVER () AS avg_episode_views,");
        sql.append(" total_episode_views - AVG(total_episode_views) OVER () AS diff_from_avg");
        sql.append(" FROM SeriesViews");
        sql.append(" WHERE 1=1");

        if(!CommonUtil.isNullOrEmpty(request.getTitle())) {
            sql.append(" AND  (title ILIKE '%' || :searchTerm || '%' OR code ILIKE :searchTerm) ");
            params.put("searchTerm", request.getTitle());
        }

        String orderBy = " ORDER BY total_episode_views DESC;";
        return query.findPagination(sql.toString(), orderBy, params, MovieResponse.class, pageable.getPageSize(), req);
    }

    @Query(value = "SELECT id, title, description, release_date, rating_score, view_count, poster_url, is_available, code, 'SERIES' AS type " +
            "FROM series " +
            "WHERE (title ILIKE '%' || :searchTerm || '%' OR code ILIKE :searchTerm) AND is_available = TRUE AND deleted_at IS NULL " +
            "UNION " +
            "SELECT id, title, description,release_date, rating_score, view_count, poster_url, is_available, code, 'MOVIE' AS type " +
            "FROM movies " +
            "WHERE (title ILIKE '%' || :searchTerm || '%' OR code ILIKE :searchTerm) AND is_available = TRUE AND deleted_at IS NULL " +
            "ORDER BY title", nativeQuery = true)
    List<ContentResponse> searchContent(@Param("searchTerm") String searchTerm);

}
