package io.watch.movie.dto.mapper;

import io.watch.movie.dto.request.ActorRequest;
import io.watch.movie.dto.response.ActorResponse;
import io.watch.movie.entity.Actor;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ActorMapper {

    // Request → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Actor toEntity(ActorRequest request);

    // Entity → Response
    ActorResponse toResponse(Actor actor);

    // List<Entity> → List<Response>
    List<ActorResponse> toResponseList(List<Actor> actors);

    // Cập nhật dữ liệu cho entity đang có
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateActorFromRequest(ActorRequest request, @MappingTarget Actor actor);
}