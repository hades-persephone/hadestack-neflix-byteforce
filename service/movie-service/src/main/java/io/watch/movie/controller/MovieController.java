package io.watch.movie.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.watch.basedata.dto.DataResults;
import io.watch.movie.dto.request.MovieRequest;
import io.watch.movie.dto.request.MovieRequestSearch;
import io.watch.movie.dto.response.MovieResponse;
import io.watch.movie.entity.Movie;
import io.watch.movie.response.ApiResponseEntity;
import io.watch.movie.response.ResponseBuilder;
import io.watch.movie.service.MovieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<ApiResponseEntity<Object>> createMovie(@Valid @RequestBody MovieRequest request, HttpServletRequest req) throws JsonProcessingException {
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
    public ResponseEntity<ApiResponseEntity<Page<Movie>>> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            HttpServletRequest req) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return responseBuilder.success(movieService.getAllMovies(pageable), req);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a movie")
    public ResponseEntity<MovieResponse> updateMovie(@PathVariable UUID id, @Valid @RequestBody MovieRequest request) throws JsonProcessingException {
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
    public ResponseEntity<ApiResponseEntity<Page<MovieResponse>>> searchMovies(
            @RequestParam(defaultValue = "") String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            HttpServletRequest req) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return responseBuilder.success(movieService.searchMoviesByTitle(title, pageable), req);
    }

    @GetMapping("/search-query")
    @Operation(summary = "Search movies by title")
    public ResponseEntity<ApiResponseEntity<DataResults<MovieResponse>>> searchMovies(
            @ModelAttribute MovieRequestSearch request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest req) {
        Pageable pageable = PageRequest.of(page, size);
        return responseBuilder.success(movieService.searchMoviesByQuery(request, pageable, req), req);
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
