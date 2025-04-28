package io.watch.movie.dto.mapper;

import io.watch.movie.dto.request.SeasonRequest;
import io.watch.movie.dto.response.SeasonResponse;
import io.watch.movie.entity.Season;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {EpisodeMapper.class})
public interface SeasonMapper {

    Season toEntity(SeasonRequest request);

    SeasonResponse toResponse(Season season);
}