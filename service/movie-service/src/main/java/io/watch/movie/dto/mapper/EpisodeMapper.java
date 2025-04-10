package io.watch.movie.dto.mapper;

import io.watch.movie.dto.request.EpisodeRequest;
import io.watch.movie.dto.response.EpisodeResponse;
import io.watch.movie.entity.Episode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EpisodeMapper {

    @Mapping(target = "season", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Episode toEntity(EpisodeRequest request);

    EpisodeResponse toResponse(Episode episode);
}
