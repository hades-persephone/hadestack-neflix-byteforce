package io.watch.movie.dto.mapper;

import io.watch.movie.dto.request.WatchHistoryRequest;
import io.watch.movie.dto.response.WatchHistoryResponse;
import io.watch.movie.entity.WatchHistory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface WatchHistoryMapper {

    WatchHistory toEntity(WatchHistoryRequest dto);

    WatchHistoryResponse toResponseDTO(WatchHistory entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget WatchHistory entity, WatchHistoryRequest dto);
}