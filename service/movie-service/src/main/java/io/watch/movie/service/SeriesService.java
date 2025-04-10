package io.watch.movie.service;

import io.watch.movie.dto.request.EpisodeRequest;
import io.watch.movie.dto.request.SeasonRequest;
import io.watch.movie.dto.request.SeriesRequest;
import io.watch.movie.dto.response.EpisodeResponse;
import io.watch.movie.dto.response.SeasonResponse;
import io.watch.movie.dto.response.SeriesResponse;

import java.util.List;
import java.util.UUID;

public interface SeriesService {
    SeriesResponse createSeries(SeriesRequest request);
    SeriesResponse getSeries(UUID id);
    List<SeriesResponse> getAllSeries();
    SeriesResponse updateSeries(UUID id, SeriesRequest request);
    void deleteSeries(UUID id);
    void incrementViewCount(UUID id);
}
