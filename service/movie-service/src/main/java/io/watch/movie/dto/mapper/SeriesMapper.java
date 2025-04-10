package io.watch.movie.dto.mapper;

import io.watch.movie.dto.request.SeriesRequest;
import io.watch.movie.dto.response.SeriesResponse;
import io.watch.movie.entity.Series;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {SeasonMapper.class})
public interface SeriesMapper {

    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "actors", ignore = true)
    @Mapping(target = "directors", ignore = true)
    @Mapping(target = "languages", ignore = true)
    @Mapping(target = "subtitles", ignore = true)
    @Mapping(target = "playlists", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "seasons", source = "seasons")
    Series toEntity(SeriesRequest request);

    @Mapping(target = "categories", expression = "java(series.getCategories().stream().map(io.watch.movie.entity.Category::getName).collect(java.util.stream.Collectors.toSet()))")
    @Mapping(target = "actors", expression = "java(series.getActors().stream().map(io.watch.movie.entity.Actor::getName).collect(java.util.stream.Collectors.toSet()))")
    @Mapping(target = "directors", expression = "java(series.getDirectors().stream().map(io.watch.movie.entity.Director::getName).collect(java.util.stream.Collectors.toSet()))")
    @Mapping(target = "languages", expression = "java(series.getLanguages().stream().map(io.watch.movie.entity.Language::getName).collect(java.util.stream.Collectors.toSet()))")
    @Mapping(target = "subtitles", expression = "java(series.getSubtitles().stream().map(io.watch.movie.entity.Subtitle::getName).collect(java.util.stream.Collectors.toSet()))")
    @Mapping(target = "playlists", expression = "java(series.getPlaylists().stream().map(io.watch.movie.entity.Playlist::getName).collect(java.util.stream.Collectors.toSet()))")
    SeriesResponse toResponse(Series series);
}