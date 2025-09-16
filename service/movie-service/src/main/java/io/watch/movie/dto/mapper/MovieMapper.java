package io.watch.movie.dto.mapper;


import io.watch.movie.dto.request.MovieRequest;
import io.watch.movie.dto.response.MovieResponse;
import io.watch.movie.entity.Movie;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MovieMapper {

    MovieMapper INSTANCE = Mappers.getMapper(MovieMapper.class);

    Movie toEntity(MovieRequest request);

    MovieResponse toResponse(Movie movie);
}