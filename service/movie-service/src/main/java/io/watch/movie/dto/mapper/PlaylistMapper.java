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

    Playlist toEntity(PlaylistRequest request);
    PlaylistResponse toResponse(Playlist playlist);
}