package io.watch.movie.dto.mapper;

import io.watch.movie.dto.request.EpisodeRequest;
import io.watch.movie.dto.response.EpisodeResponse;
import io.watch.movie.entity.Episode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EpisodeMapper {

    Episode toEntity(EpisodeRequest request);

    EpisodeResponse toResponse(Episode episode);
}
