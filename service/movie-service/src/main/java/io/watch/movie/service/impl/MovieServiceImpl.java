package io.watch.movie.service.impl;

import io.watch.movie.dto.mapper.MovieMapper;
import io.watch.movie.dto.request.MovieRequest;
import io.watch.movie.dto.response.MovieResponse;
import io.watch.movie.entity.Category;
import io.watch.movie.entity.Movie;
import io.watch.movie.exception.MovieNotFoundException;
import io.watch.movie.repository.*;
import io.watch.movie.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final CategoryRepository categoryRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MovieMapper movieMapper;

    @Override
    @Transactional
    @CacheEvict(value = "movies", allEntries = true)
    public MovieResponse createMovie(MovieRequest request) {
        validateMovieRequest(request);

        Movie movie = movieMapper.toEntity(request);
        movie.setCategories(fetchCategories(request.getCategoryIds()));

        movie = movieRepository.save(movie);
        sendNotification("NEW_MOVIE", "Phim mới: " + movie.getTitle());

        log.info("Created movie with ID: {}", movie.getId());
        return movieMapper.toResponse(movie);
    }

    @Override
    @Cacheable(value = "movies", key = "#id")
    public MovieResponse getMovie(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + id));
        return movieMapper.toResponse(movie);
    }

    @Override
    @Cacheable(value = "movies", key = "'all'")
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findByIsAvailableTrue().stream()
                .map(movieMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "movies", allEntries = true)
    public MovieResponse updateMovie(UUID id, MovieRequest request) {
        validateMovieRequest(request);

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + id));

        movie = movieMapper.toEntity(request);
        movie.setCategories(fetchCategories(request.getCategoryIds()));

        movie = movieRepository.save(movie);
        sendNotification("UPDATE_MOVIE", "Phim cập nhật: " + movie.getTitle());

        log.info("Updated movie with ID: {}", movie.getId());
        return movieMapper.toResponse(movie);
    }

    @Override
    @Transactional
    @CacheEvict(value = "movies", allEntries = true)
    public void deleteMovie(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + id));
        movie.setDeletedAt(LocalDateTime.now());
        movie.setIsAvailable(false);
        movieRepository.save(movie);

        log.info("Deleted movie with ID: {}", id);
    }

    @Override
    public List<MovieResponse> searchMoviesByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        return movieRepository.findByTitleContainingIgnoreCase(title).stream()
                .filter(Movie::getIsAvailable)
                .map(movieMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "movies", key = "'category_' + #categoryId")
    public List<MovieResponse> getMoviesByCategory(UUID categoryId) {
        return movieRepository.findByCategoryId(categoryId).stream()
                .filter(Movie::getIsAvailable)
                .map(movieMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "movies", key = "#movieId")
    public void incrementViewCount(UUID movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + movieId));
        movie.setViewCount(movie.getViewCount() + 1);
        movieRepository.save(movie);

        log.info("Incremented view count for movie ID: {}", movieId);
    }

    private void validateMovieRequest(MovieRequest request) {
        if (request.getDuration() < 1) {
            throw new IllegalArgumentException("Duration must be at least 1 minute");
        }
        if (request.getRatingScore() != null && (request.getRatingScore() < 0 || request.getRatingScore() > 10)) {
            throw new IllegalArgumentException("Rating score must be between 0 and 10");
        }
    }

    private Set<Category> fetchCategories(Set<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Set.of();
        }
        Set<Category> categories = categoryIds.stream()
                .map(categoryRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toSet());
        if (categories.size() != categoryIds.size()) {
            throw new IllegalArgumentException("One or more category IDs are invalid");
        }
        return categories;
    }

    private Double calculateRatingScore(UUID movieId, Double newScore) {
        // Logic mẫu, cần tích hợp bảng reviews nếu có
        return newScore != null ? newScore : 0.0;
    }

    private void sendNotification(String type, String message) {
        kafkaTemplate.send("movie-notifications", type, message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("❌ Kafka send failed: {} - {}, reason: {}", type, message, ex.getMessage());
                    } else {
                        log.info("✅ Kafka sent: {} - {}", type, message);
                    }
                });
    }
}
