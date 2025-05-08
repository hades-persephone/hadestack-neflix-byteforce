package io.watch.movie.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.watch.basedata.dto.DataResults;
import io.watch.movie.dto.request.MovieRequest;
import io.watch.movie.dto.request.MovieRequestSearch;
import io.watch.movie.dto.response.MovieResponse;
import io.watch.movie.entity.Movie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MovieService {
    MovieResponse createMovie(MovieRequest request) throws JsonProcessingException;
    MovieResponse getMovie(UUID id);
    Page<Movie> getAllMovies(Pageable pageable);
    MovieResponse updateMovie(UUID id, MovieRequest request) throws JsonProcessingException;
    void deleteMovie(UUID id);
    Page<MovieResponse> searchMoviesByTitle(String title, Pageable pageable);
    DataResults<MovieResponse> searchMoviesByQuery(MovieRequestSearch request, Pageable pageable, HttpServletRequest req) throws NoSuchMethodException;
    List<MovieResponse> getMoviesByCategory(UUID categoryId);
    void incrementViewCount(UUID movieId);
    byte[] exportMoviesTemplate(HttpServletRequest req);
}
