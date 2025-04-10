package io.watch.movie.dto.mapper;

import io.watch.movie.dto.request.DirectorRequest;
import io.watch.movie.dto.response.DirectorResponse;
import io.watch.movie.entity.Director;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DirectorMapper {

    Director toEntity(DirectorRequest request);

    DirectorResponse toResponse(Director director);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Director director, DirectorRequest request);
}
