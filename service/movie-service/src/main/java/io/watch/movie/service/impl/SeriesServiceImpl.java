package io.watch.movie.service.impl;

import io.watch.movie.dto.mapper.EpisodeMapper;
import io.watch.movie.dto.mapper.SeasonMapper;
import io.watch.movie.dto.mapper.SeriesMapper;
import io.watch.movie.dto.request.SeriesRequest;
import io.watch.movie.dto.response.SeriesResponse;
import io.watch.movie.entity.*;
import io.watch.movie.exception.ResourceNotFoundException;
import io.watch.movie.repository.*;
import io.watch.movie.service.SeriesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class SeriesServiceImpl implements SeriesService {

    private final SeriesRepository seriesRepository;
    private final SeasonRepository seasonRepository;
    private final EpisodeRepository episodeRepository;
    private final CategoryRepository categoryRepository;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;
    private final LanguageRepository languageRepository;
    private final SubtitleRepository subtitleRepository;
    private final PlaylistRepository playlistRepository;

    private final SeriesMapper seriesMapper;
    private final SeasonMapper seasonMapper;
    private final EpisodeMapper episodeMapper;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    @Transactional
    @CacheEvict(value = "series", allEntries = true)
    public SeriesResponse createSeries(SeriesRequest request) {
        Series series = seriesMapper.toEntity(request);
        fetchRelationships(request, series);

        series = getSeries(request, series);

        sendNotification("NEW_SERIES", "Series phim mới: " + series);

        return seriesMapper.toResponse(series);
    }

    @Override
    @Cacheable(value = "series", key = "#id")
    public SeriesResponse getSeries(UUID id) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Series not found with ID: " + id));
        return seriesMapper.toResponse(series);
    }

    @Override
    @Cacheable(value = "series", key = "'all'")
    public List<SeriesResponse> getAllSeries() {
        return seriesRepository.findAll().stream()
                .filter(Series::getIsAvailable)
                .map(seriesMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "series", allEntries = true)
    public SeriesResponse updateSeries(UUID id, SeriesRequest request) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Series not found with ID: " + id));

        // Update basic series info
        series.setTitle(request.getTitle());
        series.setDescription(request.getDescription());
        series.setReleaseDate(request.getReleaseDate());
        series.setRatingScore(request.getRatingScore());
        series.setImdbRating(request.getImdbRating());
        series.setRottenTomatoesScore(request.getRottenTomatoesScore());
        series.setProductionCompany(request.getProductionCompany());
        series.setTrailerUrl(request.getTrailerUrl());
        series.setPosterUrl(request.getPosterUrl());
        series.setThumbnailUrl(request.getThumbnailUrl());
        series.setAgeRating(request.getAgeRating());
        series.setCountryOfOrigin(request.getCountryOfOrigin());

        // Update relationships
        fetchRelationships(request, series);

        // Clear and update seasons
        series.getSeasons().forEach(season -> season.getEpisodes().clear());
        series.getSeasons().clear();
        series = getSeries(request, series);
        log.info("Updated series with ID: {}", id);

        sendNotification("UPDATE_SERIES", "Update series phim: " + series.getTitle());

        return seriesMapper.toResponse(series);
    }

    private Series getSeries(SeriesRequest request, Series series) {
        if (request.getSeasons() != null && !request.getSeasons().isEmpty()) {
            Series finalSeries = series;
            Set<Season> seasons = request.getSeasons().stream()
                    .map(seasonRequest -> {
                        Season season = seasonMapper.toEntity(seasonRequest);
                        season.setSeries(finalSeries);
                        if (seasonRequest.getEpisodes() != null && !seasonRequest.getEpisodes().isEmpty()) {
                            Set<Episode> episodes = seasonRequest.getEpisodes().stream()
                                    .map(episodeMapper::toEntity)
                                    .peek(episode -> episode.setSeason(season))
                                    .collect(Collectors.toSet());
                            season.setEpisodes(episodes);
                        }
                        return season;
                    })
                    .collect(Collectors.toSet());
            series.setSeasons(seasons);
        }

        series = seriesRepository.save(series);
        return series;
    }

    private void fetchRelationships(SeriesRequest request, Series series) {
        series.setCategories(fetchCategories(request.getCategoryIds()));
        series.setActors(fetchActors(request.getActorIds()));
        series.setDirectors(fetchDirectors(request.getDirectorIds()));
        series.setLanguages(fetchLanguages(request.getLanguageIds()));
        series.setSubtitles(fetchSubtitles(request.getSubtitleIds()));
        series.setPlaylists(fetchPlaylists(request.getPlaylistIds()));
    }

    @Override
    @Transactional
    @CacheEvict(value = "series", allEntries = true)
    public void deleteSeries(UUID id) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Series not found with ID: " + id));
        series.setDeletedAt(LocalDateTime.now());
        series.setIsAvailable(false);
        seriesRepository.save(series);
        log.info("Deleted series with ID: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "series", key = "#id")
    public void incrementViewCount(UUID id) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Series not found with ID: " + id));
        series.setViewCount(series.getViewCount() + 1);
        seriesRepository.save(series);
        log.info("Incremented view count for series ID: {}", id);
    }

    // Fetch methods for relationships
    private Set<Category> fetchCategories(Set<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(categoryIds));
        if (categories.size() != categoryIds.size()) {
            throw new ResourceNotFoundException("One or more categories not found");
        }
        return categories;
    }

    private Set<Actor> fetchActors(Set<UUID> actorIds) {
        if (actorIds == null || actorIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Actor> actors = new HashSet<>(actorRepository.findAllById(actorIds));
        if (actors.size() != actorIds.size()) {
            throw new ResourceNotFoundException("One or more actors not found");
        }
        return actors;
    }

    private Set<Director> fetchDirectors(Set<UUID> directorIds) {
        if (directorIds == null || directorIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Director> directors = new HashSet<>(directorRepository.findAllById(directorIds));
        if (directors.size() != directorIds.size()) {
            throw new ResourceNotFoundException("One or more directors not found");
        }
        return directors;
    }

    private Set<Language> fetchLanguages(Set<UUID> languageIds) {
        if (languageIds == null || languageIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Language> languages = new HashSet<>(languageRepository.findAllById(languageIds));
        if (languages.size() != languageIds.size()) {
            throw new ResourceNotFoundException("One or more languages not found");
        }
        return languages;
    }

    private Set<Subtitle> fetchSubtitles(Set<UUID> subtitleIds) {
        if (subtitleIds == null || subtitleIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Subtitle> subtitles = new HashSet<>(subtitleRepository.findAllById(subtitleIds));
        if (subtitles.size() != subtitleIds.size()) {
            throw new ResourceNotFoundException("One or more subtitles not found");
        }
        return subtitles;
    }

    private Set<Playlist> fetchPlaylists(Set<UUID> playlistIds) {
        if (playlistIds == null || playlistIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Playlist> playlists = new HashSet<>(playlistRepository.findAllById(playlistIds));
        if (playlists.size() != playlistIds.size()) {
            throw new ResourceNotFoundException("One or more playlists not found");
        }
        return playlists;
    }


    private void sendNotification(String type, String message) {
        kafkaTemplate.send("series-notifications", type, message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("❌ Kafka send failed: {} - {}, reason: {}", type, message, ex.getMessage());
                    } else {
                        log.info("✅ Kafka sent: {} - {}", type, message);
                    }
                });
    }
}
