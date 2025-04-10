package io.watch.movie.service;

import io.watch.movie.dto.request.MovieRequest;
import io.watch.movie.dto.response.MovieResponse;

import java.util.List;
import java.util.UUID;

public interface MovieService {
    MovieResponse createMovie(MovieRequest request);
    MovieResponse getMovie(UUID id);
    List<MovieResponse> getAllMovies();
    MovieResponse updateMovie(UUID id, MovieRequest request);
    void deleteMovie(UUID id);
    List<MovieResponse> searchMoviesByTitle(String title);
    List<MovieResponse> getMoviesByCategory(UUID categoryId);
    void incrementViewCount(UUID movieId);
}
