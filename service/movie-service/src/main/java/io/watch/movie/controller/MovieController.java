package io.watch.movie.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.watch.movie.dto.request.MovieRequest;
import io.watch.movie.dto.response.MovieResponse;
import io.watch.movie.response.ApiResponseEntity;
import io.watch.movie.response.ResponseBuilder;
import io.watch.movie.service.MovieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final ResponseBuilder responseBuilder;

    @PostMapping
    @Operation(summary = "Create a new movie")
    public ResponseEntity<ApiResponseEntity<Object>> createMovie(@Valid @RequestBody MovieRequest request, HttpServletRequest req) {
        movieService.createMovie(request);
        return responseBuilder.successMessage("Created a new movie", req);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a movie by ID")
    public ResponseEntity<ApiResponseEntity<MovieResponse>> getMovie(@PathVariable(name = "id") UUID id, HttpServletRequest req) {
        return responseBuilder.success(movieService.getMovie(id), req);
    }

    @GetMapping
    @Operation(summary = "Get all available movies")
    public ResponseEntity<ApiResponseEntity<List<MovieResponse>>> getAllMovies(HttpServletRequest req) {
        return responseBuilder.success(movieService.getAllMovies(), req);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a movie")
    public ResponseEntity<MovieResponse> updateMovie(@PathVariable UUID id, @Valid @RequestBody MovieRequest request) {
        return ResponseEntity.ok(movieService.updateMovie(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a movie (soft delete)")
    public ResponseEntity<ApiResponseEntity<Object>> deleteMovie(@PathVariable UUID id, HttpServletRequest req) {
        movieService.deleteMovie(id);
        return responseBuilder.successMessage("Created a new movie", req);
    }

    @GetMapping("/search")
    @Operation(summary = "Search movies by title")
    public ResponseEntity<ApiResponseEntity<List<MovieResponse>>> searchMovies(@RequestParam String title, HttpServletRequest req) {
        return responseBuilder.success(movieService.searchMoviesByTitle(title), req);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get movies by category ID")
    public ResponseEntity<ApiResponseEntity<List<MovieResponse>>> getMoviesByCategory(@PathVariable UUID categoryId, HttpServletRequest req) {
        return responseBuilder.success(movieService.getMoviesByCategory(categoryId), req);
    }

    @PostMapping("/{id}/view")
    @Operation(summary = "Increment view count for a movie")
    public ResponseEntity<Void> incrementViewCount(@PathVariable UUID id, HttpServletRequest req) {
        movieService.incrementViewCount(id);
        return ResponseEntity.ok().build();

    }
}
