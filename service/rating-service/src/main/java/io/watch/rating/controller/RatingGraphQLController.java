package io.watch.rating.controller;

import io.watch.rating.dto.RatingCreateRequest;
import io.watch.rating.dto.RatingResponse;
import io.watch.rating.entity.RatingStatistics;
import io.watch.rating.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class RatingGraphQLController {

    private final RatingService ratingService;

    @QueryMapping
    public Map<String, Object> ratingsByMovie(@Argument UUID movieId, @Argument int page, @Argument int size) {
        Pageable pageable = PageRequest.of(page, size);
        Slice<RatingResponse> ratingsSlice = ratingService.getRatingsByMovie(movieId, pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("content", ratingsSlice.getContent());
        result.put("hasNext", ratingsSlice.hasNext());
        result.put("hasPrevious", ratingsSlice.hasPrevious());
        result.put("number", ratingsSlice.getNumber());
        result.put("size", ratingsSlice.getSize());

        // For totalElements and totalPages, we need to convert Slice to Page
        // This is a simplification, in a real app you might want to get the actual count
        if (ratingsSlice instanceof Page<RatingResponse> ratingsPage) {
            result.put("totalElements", ratingsPage.getTotalElements());
            result.put("totalPages", ratingsPage.getTotalPages());
        } else {
            // Default values if not a Page
            result.put("totalElements", ratingsSlice.getContent().size());
            // Use the page parameter for a simple estimation of total pages
            result.put("totalPages", ratingsSlice.hasNext() ? page + 2 : page + 1);
        }

        return result;
    }

    @QueryMapping
    public Double averageRating(@Argument UUID movieId) {
        return ratingService.getAverageRating(movieId);
    }

    @QueryMapping
    public RatingStatistics ratingStatistics(@Argument UUID movieId) {
        return ratingService.getRatingStatistics(movieId);
    }

    @MutationMapping
    public RatingResponse submitRating(@Argument("request") RatingCreateRequest request) {
        return ratingService.submittingRating(request);
    }

    @MutationMapping
    public Boolean deleteUserRatings(@Argument UUID userId) {
        ratingService.deleteUserRatings(userId);
        return true;
    }
}
