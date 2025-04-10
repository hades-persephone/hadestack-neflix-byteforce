package io.watch.movie.dto.mapper;

import io.watch.movie.dto.request.LanguageRequest;
import io.watch.movie.dto.response.LanguageResponse;
import io.watch.movie.entity.Language;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LanguageMapper {

    Language toEntity(LanguageRequest request);

    LanguageResponse toResponse(Language language);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Language language, LanguageRequest request);
}