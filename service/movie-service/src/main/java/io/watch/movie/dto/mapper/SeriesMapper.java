package io.watch.movie.dto.mapper;

import io.watch.movie.dto.request.SeriesRequest;
import io.watch.movie.dto.response.SeriesResponse;
import io.watch.movie.entity.Series;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {SeasonMapper.class})
public interface SeriesMapper {

    Series toEntity(SeriesRequest request);
    SeriesResponse toResponse(Series series);
}