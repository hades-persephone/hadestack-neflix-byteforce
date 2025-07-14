package io.watch.rating.handler;

import graphql.kickstart.tools.GraphQLMutationResolver;
import graphql.kickstart.tools.GraphQLQueryResolver;
import io.watch.rating.dto.RatingCreateRequest;
import io.watch.rating.dto.RatingResponse;
import io.watch.rating.entity.RatingStatistics;
import io.watch.rating.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GraphQLResolver implements GraphQLQueryResolver, GraphQLMutationResolver {

    private final RatingService ratingService;

    public Slice<RatingResponse> ratingByMovie(UUID movieId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ratingService.getRatingsByMovie(movieId, pageable);
    }

    public double averageRating(UUID movieId) {
        return ratingService.getAverageRating(movieId);
    }

    private RatingStatistics ratingStatistics(UUID movieId) {
        return ratingService.getRatingStatistics(movieId);
    }

    public RatingResponse submitRating(RatingCreateRequest request) {
        return ratingService.submittingRating(request);
    }

}
