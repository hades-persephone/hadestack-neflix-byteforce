package io.watch.movie.dto.mapper;

import io.watch.movie.dto.request.CategoryRequest;
import io.watch.movie.dto.response.CategoryResponse;
import io.watch.movie.entity.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(CategoryRequest request);

    CategoryResponse toResponse(Category category);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Category category, CategoryRequest request);
}
