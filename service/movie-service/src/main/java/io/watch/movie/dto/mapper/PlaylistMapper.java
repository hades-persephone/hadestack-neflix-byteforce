package io.watch.movie.dto.mapper;


import io.watch.movie.dto.request.PlaylistRequest;
import io.watch.movie.dto.response.PlaylistResponse;
import io.watch.movie.entity.Playlist;
import io.watch.movie.entity.substraction.Visibility;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PlaylistMapper {

    PlaylistMapper INSTANCE = Mappers.getMapper(PlaylistMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "visibility", expression = "java(Visibility.valueOf(request.getVisibility()))")
    Playlist toEntity(PlaylistRequest request);

    @Mapping(target = "visibility", expression = "java(playlist.getVisibility().name())")
    PlaylistResponse toResponse(Playlist playlist);
}