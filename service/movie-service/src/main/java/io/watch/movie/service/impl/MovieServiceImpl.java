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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private static final Logger log = LoggerFactory.getLogger(MovieServiceImpl.class);

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

    private static final String MOVIE_NOT_FOUND = "Movie not found with ID: ";

    @Override
    @ReadOnly
    @Cacheable(value = "movies", keyGenerator = "customCacheKeyGenerator")
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
            redisTemplate.opsForValue().set(generatedKey, result, 60, TimeUnit.MINUTES);
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
    @CacheEvict(value = "movies", allEntries = true)
    public MovieResponse createMovie(MovieRequest request) throws JsonProcessingException {
        validateMovieRequest(request);

        Movie movie = movieMapper.toEntity(request);
        movie.setCategories(fetchCategories(request.getCategoryIds()));
        movie.setActors(fetchActors(request.getActorIds()));
        movie.setDirectors(fetchDirectors(request.getDirectorIds()));
        movie.setLanguages(fetchLanguages(request.getLanguageIds()));

        movie = movieRepository.save(movie);
        sendNotification("NEW_MOVIE", "Phim mới: " + movie.getTitle());

        log.info("Created movie with ID: {}", movie.getId());
        return movieMapper.toResponse(movie);
    }

    @Override
    @Cacheable(value = "movies", key = "#id")
    public MovieResponse getMovie(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND + id));
        return movieMapper.toResponse(movie);
    }

    @Override
    @Cacheable(value = "movies", key = "'all'")
    public Page<Movie> getAllMovies(Pageable pageable) {
        return movieRepository.findAll(pageable);
    }

    @Override
    @Transactional
    @CacheEvict(value = "movies", allEntries = true)
    public MovieResponse updateMovie(UUID id, MovieRequest request) throws JsonProcessingException {
        validateMovieRequest(request);

        movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND + id));
        Movie movie;

        movie = movieMapper.toEntity(request);
        movie.setCategories(fetchCategories(request.getCategoryIds()));
        movie.setActors(fetchActors(request.getActorIds()));
        movie.setDirectors(fetchDirectors(request.getDirectorIds()));
        movie.setLanguages(fetchLanguages(request.getLanguageIds()));

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
    @Cacheable(value = "movies", key = "'category_' + #categoryId")
    public List<MovieResponse> getMoviesByCategory(UUID categoryId) {
        return movieRepository.findByCategoriesId(categoryId).stream()
                .filter(Movie::getIsAvailable)
                .map(movieMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "movies", key = "#movieId")
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
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        if (categories.size() != categoryIds.size()) {
            throw new IllegalArgumentException("One or more category IDs are invalid");
        }
        return categories;
    }

    private Set<Actor> fetchActors(Set<UUID> actorsIds) {
        if (actorsIds == null || actorsIds.isEmpty()) {
            return Set.of();
        }
        Set<Actor> actors = actorsIds.stream()
                .map(actorRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        if (actors.size() != actorsIds.size()) {
            throw new IllegalArgumentException("One or more actor IDs are invalid");
        }
        return actors;
    }

    private Set<Director> fetchDirectors(Set<UUID> directorsIds) {
        if (directorsIds == null || directorsIds.isEmpty()) {
            return Set.of();
        }
        Set<Director> directors = directorsIds.stream()
                .map(directorRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        if (directors.size() != directorsIds.size()) {
            throw new IllegalArgumentException("One or more directors IDs are invalid");
        }
        return directors;
    }

    private Set<Language> fetchLanguages(Set<UUID> languagesIds) {
        if (languagesIds == null || languagesIds.isEmpty()) {
            return Set.of();
        }
        Set<Language> languages = languagesIds.stream()
                .map(languageRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        if (languages.size() != languagesIds.size()) {
            throw new IllegalArgumentException("One or more languages IDs are invalid");
        }
        return languages;
    }


    private Double calculateRatingScore(UUID movieId, Double newScore) {
        // Logic mẫu, cần tích hợp bảng reviews nếu có
        return newScore != null ? newScore : 0.0;
    }

    private void sendNotification(String type, String message) throws JsonProcessingException {
        MovieNotificationDTO sendMessage = new MovieNotificationDTO(type, message);
        String json = objectMapper.writeValueAsString(sendMessage);
        kafkaTemplate.send("movie-notifications", type, json)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("❌ Kafka send failed: {} - {}, reason: {}", type, json, ex.getMessage());
                    } else {
                        log.info("✅ Kafka sent: {} - {}", type, json);
                    }
                });
    }
}
