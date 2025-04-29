package io.watch.movie.repository;

import io.watch.basedata.data.common.BaseNativeQueryExecutor;
import io.watch.basedata.dto.DataResults;
import io.watch.basedata.util.CommonUtil;
import io.watch.movie.dto.request.MovieRequest;
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
import reactor.util.annotation.NonNullApi;

import java.util.*;

@EnableRedisRepositories
@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {
    Page<Movie> findAll(Pageable pageable);
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    List<Movie> findByCategoriesId(UUID categoryId);

    @Query("SELECT m FROM Movie m WHERE m.isAvailable = true AND m.id = ?1")
    Optional<Movie> findByIdAndIsAvailableTrue(UUID uuid);

    public default DataResults<MovieResponse> search(BaseNativeQueryExecutor query, MovieRequest request, Pageable pageable, HttpServletRequest req) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT                                  ");
        sql.append(" mo.id as id,                           ");
        sql.append(" mo.title as title,                     ");
        sql.append(" mo.release_date as releaseDate,        ");
        sql.append(" mo.rating_score as ratingScore,        ");
        sql.append(" mo.poster_url as posterUrl,            ");
        sql.append(" mo.age_rating as ageRating,            ");
        sql.append(" mo.duration as duration,               ");
        sql.append(" FROM movie mo                          ");
        sql.append(" WHERE 1=1 AND is_available = TRUE AND deleted_at IS NULL ");

        if(!CommonUtil.isEmpty(request)) {
            if(!CommonUtil.isNullOrEmpty(request.getTitle())) {
                sql.append(" AND (mo.title LIKE CONCAT('%', :title, '%') OR mo.description LIKE CONCAT('%', :title, '%') ) ");
                params.put("title", request.getTitle());
            }

            if(!CommonUtil.isNullOrEmpty(request.getReleaseStartDate().toString()) && !CommonUtil.isNullOrEmpty(request.getReleaseStartDate().toString())) {
                sql.append(" AND release_date BETWEEN :startDate AND :endDate ");
                params.put("startDate", request.getReleaseStartDate().toString());
                params.put("endDate", request.getReleaseDate().toString());
            } else {
                if(!CommonUtil.isNullOrEmpty(request.getReleaseStartDate().toString())) {
                    sql.append(" AND release_date >= :startDate ");
                    params.put("startDate", request.getReleaseStartDate().toString());
                }
                if(!CommonUtil.isNullOrEmpty(request.getReleaseStartDate().toString())) {
                    sql.append(" AND release_date <= :endDate ");
                    params.put("endDate", request.getReleaseDate().toString());
                }
            }
        }
        String order = " ORDER BY mo.release_date DESC, mo.id ASC ";
        return query.findPagination(sql.toString(), order, params, MovieResponse.class, pageable.getPageSize(), req);
    }

    public default DataResults<MovieResponse> seriesView(BaseNativeQueryExecutor query, MovieRequest request, Pageable pageable, HttpServletRequest req) {
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
