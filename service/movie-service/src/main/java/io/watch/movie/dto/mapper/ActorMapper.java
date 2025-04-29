package io.watch.movie.dto.mapper;

import io.watch.movie.dto.request.ActorRequest;
import io.watch.movie.dto.response.ActorResponse;
import io.watch.movie.entity.Actor;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ActorMapper {

    Actor toEntity(ActorRequest request);

    ActorResponse toResponse(Actor actor);
    List<ActorResponse> toResponseList(List<Actor> actors);
    void updateActorFromRequest(ActorRequest request, @MappingTarget Actor actor);
}