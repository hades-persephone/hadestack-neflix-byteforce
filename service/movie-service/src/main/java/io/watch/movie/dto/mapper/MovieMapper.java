package io.watch.movie.dto.mapper;


import io.watch.movie.dto.request.MovieRequest;
import io.watch.movie.dto.response.MovieResponse;
import io.watch.movie.entity.Movie;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MovieMapper {

    MovieMapper INSTANCE = Mappers.getMapper(MovieMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "actors", ignore = true)
    @Mapping(target = "directors", ignore = true)
    @Mapping(target = "languages", ignore = true)
    @Mapping(target = "subtitles", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Movie toEntity(MovieRequest request);

    @Mapping(target = "categories", expression = "java(movie.getCategories().stream().map(c -> c.getName()).collect(java.util.stream.Collectors.toSet()))")
    @Mapping(target = "actors", expression = "java(movie.getActors().stream().map(a -> a.getName()).collect(java.util.stream.Collectors.toSet()))")
    @Mapping(target = "directors", expression = "java(movie.getDirectors().stream().map(d -> d.getName()).collect(java.util.stream.Collectors.toSet()))")
    @Mapping(target = "languages", expression = "java(movie.getLanguages().stream().map(l -> l.getName()).collect(java.util.stream.Collectors.toSet()))")
    @Mapping(target = "subtitles", expression = "java(movie.getSubtitles().stream().map(s -> s.getName()).collect(java.util.stream.Collectors.toSet()))")
    MovieResponse toResponse(Movie movie);
}