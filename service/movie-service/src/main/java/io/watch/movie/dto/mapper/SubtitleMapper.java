package io.watch.movie.dto.mapper;

import io.watch.movie.dto.request.SubtitleRequest;
import io.watch.movie.dto.response.SubtitleResponse;
import io.watch.movie.entity.Subtitle;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SubtitleMapper {

    Subtitle toEntity(SubtitleRequest dto);

    SubtitleResponse toResponseDTO(Subtitle subtitle);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Subtitle subtitle, SubtitleRequest dto);
}
