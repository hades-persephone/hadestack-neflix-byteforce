package io.watch.movie.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.watch.basedata.data.common.BaseNativeQueryExecutor;
import io.watch.basedata.dto.DataResults;
import io.watch.movie.config.cache.CustomCacheKeyGenerator;
import io.watch.movie.dto.MovieNotificationDTO;
import io.watch.movie.dto.mapper.MovieMapper;
import io.watch.movie.dto.request.MovieRequest;
import io.watch.movie.dto.request.MovieRequestSearch;
import io.watch.movie.dto.response.MovieResponse;
import io.watch.movie.entity.*;
import io.watch.movie.exception.MovieNotFoundException;
import io.watch.movie.handler.MovieCodeGenerator;
import io.watch.movie.handler.kafka.KafkaErrorHandlerService;
import io.watch.movie.repository.*;
import io.watch.movie.service.MovieService;
import io.watch.movie.util.ReadOnly;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieServiceImpl implements MovieService {

    private final KafkaErrorHandlerService kafkaErrorHandlerService;
    @Value("${cache.movie.default.ttl}")
    private Long DEFAULT_TTL_MINUTES;

    @Value("${cache.movie.popular.ttl}")
    private Long POPULAR_MOVIE_TTL_MINUTES;

    private final MovieRepository movieRepository;
    private final CategoryRepository categoryRepository;
    private final SubtitleRepository subtitleRepository;
    private final DirectorRepository directorRepository;
    private final LanguageRepository languageRepository;
    private final ActorRepository actorRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MovieMapper movieMapper;
    private final BaseNativeQueryExecutor query;
    private final ObjectMapper objectMapper;
    private final CustomCacheKeyGenerator keyGenerator;
    private final MovieCodeGenerator codeGenerator;

    private static final String CACHE_NAME = "movies";
    private static final String MOVIE_NOT_FOUND = "Movie not found with ID: ";
    private static final String KAFKA_TOPIC_NOTIFICATION = "movie-notifications";

    @Override
    @ReadOnly
    @Cacheable(value = CACHE_NAME, keyGenerator = "customCacheKeyGenerator")
    public DataResults<MovieResponse> searchMoviesByQuery(MovieRequestSearch request, Pageable pageable, HttpServletRequest req) throws NoSuchMethodException {

        String generatedKey = (String) keyGenerator.generate(
                this,
                this.getClass().getMethod("searchMoviesByQuery", MovieRequestSearch.class, Pageable.class, HttpServletRequest.class),
                request, pageable, req
        );

        try {
            Object cachedValue = redisTemplate.opsForValue().get(generatedKey);

            if (cachedValue != null) {
                log.debug("Cache hit for key: {}", generatedKey);
                if (cachedValue instanceof DataResults) {
                    return (DataResults<MovieResponse>) cachedValue;
                }

                String json = objectMapper.writeValueAsString(cachedValue);
                DataResults<MovieResponse> converted = objectMapper.readValue(json,
                        objectMapper.getTypeFactory().constructParametricType(
                                DataResults.class, MovieResponse.class));
                log.debug("Successfully converted cache value from {} to DataResults", cachedValue.getClass().getName());
                return converted;
            }
        } catch (Exception e) {
            log.error("Error retrieving from cache: {}", e.getMessage());
            redisTemplate.delete(generatedKey);
        }
        log.debug("Cache miss for key: {}", generatedKey);
        DataResults<MovieResponse> result = movieRepository.search(query, request, pageable, req);
        try {
            long ttl = Integer.parseInt(result.getRecordsTotal()) > 100 ? DEFAULT_TTL_MINUTES : POPULAR_MOVIE_TTL_MINUTES;
            redisTemplate.opsForValue().set(generatedKey, result, ttl, TimeUnit.MINUTES);
            log.debug("Successfully cached search results");
        } catch (Exception e) {
            log.error("Error caching result: {}", e.getMessage());
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> DataResults<T> castToDataResults(Object obj) {
        if (obj instanceof DataResults<?> dataResults) {
            return (DataResults<T>) dataResults;
        }
        return null;
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public MovieResponse createMovie(MovieRequest request) throws JsonProcessingException {
        validateMovieRequest(request);

        Movie movie = movieMapper.toEntity(request);
        enrich(movie, request);
        List<Director> director = directorRepository.findByIdIn(request.getDirectorIds());
        List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
        String code = generateUniqueCode(request.getTitle(), request.getReleaseDate().getYear(), director.get(0).getFullName(), categories.get(0).getName());
        movie.setCode(code);
        movie = movieRepository.save(movie);
        sendNotification("NEW_MOVIE", "Phim mới: " + movie.getTitle());

        log.info("Created movie with ID: {}", movie.getId());
        return movieMapper.toResponse(movie);
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "#id")
    public MovieResponse getMovie(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND + id));
        return movieMapper.toResponse(movie);
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'all'")
    public Page<Movie> getAllMovies(Pageable pageable) {
        return movieRepository.findAll(pageable);
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public MovieResponse updateMovie(UUID id, MovieRequest request) throws JsonProcessingException {
        validateMovieRequest(request);

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND + id));

        movie = movieMapper.toEntity(request);
        enrich(movie, request);

        movie = movieRepository.save(movie);
        sendNotification("UPDATE_MOVIE", "Phim cập nhật: " + movie.getTitle());

        log.info("Updated movie with ID: {}", movie.getId());
        return movieMapper.toResponse(movie);
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void deleteMovie(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND + id));
        movie.setDeletedAt(LocalDateTime.now());
        movie.setIsAvailable(false);
        movieRepository.save(movie);

        log.info("Deleted movie with ID: {}", id);
    }

    @Override
    public Page<MovieResponse> searchMoviesByTitle(String title, Pageable pageable) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        Page<Movie> movieEntities = movieRepository.findByTitleContainingIgnoreCase(title, pageable);
        return movieEntities.map(movieMapper::toResponse);
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'category_' + #categoryId")
    public List<MovieResponse> getMoviesByCategory(UUID categoryId) {
        return movieRepository.findByCategoriesId(categoryId).stream()
                .filter(Movie::getIsAvailable)
                .map(movieMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAME, key = "#movieId")
    public void incrementViewCount(UUID movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND + movieId));
        movie.setViewCount(movie.getViewCount() + 1);
        movieRepository.save(movie);

        log.info("Incremented view count for movie ID: {}", movieId);
    }

    @Override
    public byte[] exportMoviesTemplate(HttpServletRequest req) {
        try (InputStream is = new ClassPathResource("/templates/export_template_import_movies.xlsx").getInputStream();
             ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
             Workbook rsWorkbook = WorkbookFactory.create(is)) {
            rsWorkbook.write(byteArr);
            return byteArr.toByteArray();
        } catch (IOException e) {
            throw new MovieNotFoundException("Export movies template failed", e);
        }
    }

    public void enrich(Movie movie, MovieRequest request) {
        movie.setCategories(fetchEntities(request.getCategoryIds(), categoryRepository, "category"));
        movie.setActors(fetchEntities(request.getActorIds(), actorRepository, "actor"));
        movie.setDirectors(fetchEntities(request.getDirectorIds(), directorRepository, "director"));
        movie.setLanguages(fetchEntities(request.getLanguageIds(), languageRepository, "language"));
        movie.setUpdatedAt(LocalDateTime.now());
    }

    private <T> Set<T> fetchEntities(Set<UUID> ids, JpaRepository<T, UUID> repository, String entityType) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }
        return ids.stream()
                .map(id -> repository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid " + entityType + " ID: " + id)))
                .collect(Collectors.toSet());
    }

    private void validateMovieRequest(MovieRequest request) {
        if (request.getRatingScore() != null && (request.getRatingScore() < 0 || request.getRatingScore() > 10)) {
            throw new IllegalArgumentException("Rating score must be between 0 and 10");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (request.getReleaseDate() != null && request.getReleaseDate().isAfter(LocalDateTime.now().toLocalDate())) {
            throw new IllegalArgumentException("Release date cannot be in the future");
        }
    }


    private Double calculateRatingScore(UUID movieId, Double newScore) {
        // Logic mẫu, cần tích hợp bảng reviews nếu có
        return newScore != null ? newScore : 0.0;
    }

    private String generateUniqueCode(String title, Integer releaseYear, String director, String genre) {
        String code = codeGenerator.generateMeaningfulCode(title, releaseYear);

        if (movieRepository.existsByCode(code)) {
            code = codeGenerator.generateShortMeaningfulCode(title, releaseYear);
        }

        if (movieRepository.existsByCode(code)) {
            code = codeGenerator.generateDirectorBasedCode(director, title, releaseYear);
        }

        if (movieRepository.existsByCode(code)) {
            code = codeGenerator.generateNetflixStyleCode(title, releaseYear);
        }

        if (movieRepository.existsByCode(code)) {
            code = codeGenerator.generateUUIDCode();
        }

        return code;
    }

    @Async
    @CircuitBreaker(name = "kafkaNotification", fallbackMethod = "sendNotificationFallback")
    public void sendNotification(String type, String message) throws JsonProcessingException {
        MovieNotificationDTO sendMessage = new MovieNotificationDTO(type, message);
        String json = objectMapper.writeValueAsString(sendMessage);
        kafkaTemplate.send(KAFKA_TOPIC_NOTIFICATION, type, json)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        Map<String, Object> headers = new HashMap<>();
                        headers.put("timestamp", LocalDateTime.now());
                        headers.put("source", this.getClass().getSimpleName());
                        kafkaErrorHandlerService.handlerKafkaError(KAFKA_TOPIC_NOTIFICATION, type, json, ex, headers);
                        log.error("❌ Kafka send failed: {} - {}, reason: {}", type, json, ex.getMessage());
                    } else {
                        log.info("✅ Kafka sent: {} - {}", type, json);
                    }
                });
    }

    public void sendNotificationFallback(String type, String message, Throwable t) {
        log.warn("Kafka notification fallback triggered for type {}, message: {}, reason: {}", type, message, t.getMessage());
    }

}
