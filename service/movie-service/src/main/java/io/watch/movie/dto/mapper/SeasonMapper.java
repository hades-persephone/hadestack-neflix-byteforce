package io.watch.movie.dto.mapper;

import io.watch.movie.dto.request.SeasonRequest;
import io.watch.movie.dto.response.SeasonResponse;
import io.watch.movie.entity.Season;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {EpisodeMapper.class})
public interface SeasonMapper {

    @Mapping(target = "series", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "episodes", source = "episodes")
    Season toEntity(SeasonRequest request);

    SeasonResponse toResponse(Season season);
}