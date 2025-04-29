package io.watch.movie.service;

import io.watch.movie.dto.request.MovieRequest;
import io.watch.movie.dto.response.MovieResponse;
import io.watch.movie.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MovieService {
    MovieResponse createMovie(MovieRequest request);
    MovieResponse getMovie(UUID id);
    Page<Movie> getAllMovies(Pageable pageable);
    MovieResponse updateMovie(UUID id, MovieRequest request);
    void deleteMovie(UUID id);
    Page<MovieResponse> searchMoviesByTitle(String title, Pageable pageable);
    List<MovieResponse> getMoviesByCategory(UUID categoryId);
    void incrementViewCount(UUID movieId);
}
