package io.watch.movie.dto.mapper;


import io.watch.movie.dto.request.ReviewRequest;
import io.watch.movie.dto.response.ReviewResponse;
import io.watch.movie.entity.Review;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    Review toEntity(ReviewRequest dto);

    ReviewResponse toResponseDTO(Review entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Review entity, ReviewRequest dto);
}