package io.watch.rating.config.graphql;


import graphql.schema.DataFetchingEnvironment;
import io.watch.rating.dto.RatingCreateRequest;
import io.watch.rating.dto.RatingMetadata;
import io.watch.rating.dto.RatingResponse;
import io.watch.rating.exception.RatingServiceException;
import io.watch.rating.service.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class RatingMutationResolver {

    private final RatingService ratingService;

    public CompletableFuture<RatingResponse> submitRating(DataFetchingEnvironment environment) {
        Object requestInput = environment.getArgument("request");
        log.debug("Submitting rating with request: {}", requestInput);

        return CompletableFuture.supplyAsync(() -> {
            try {
                RatingCreateRequest request = convertToRatingCreateRequest(requestInput);
                return ratingService.submittingRating(request);
            } catch (Exception e) {
                log.error("Error submitting rating", e);
                throw new RatingServiceException("Failed to submit rating", e);
            }
        });
    }

    public CompletableFuture<Boolean> deleteUserRatings(DataFetchingEnvironment environment) {
        String userId = environment.getArgument("userId");
        log.debug("Deleting ratings for user ID: {}", userId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID userUuid = UUID.fromString(userId);
                ratingService.deleteUserRatings(userUuid);
                return true;
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format for user ID: {}", userId, e);
                throw new RatingServiceException("Invalid user ID format", e);
            } catch (Exception e) {
                log.error("Error deleting ratings for user ID: {}", userId, e);
                throw new RatingServiceException("Failed to delete user ratings", e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private RatingCreateRequest convertToRatingCreateRequest(Object requestInput) {
        if (!(requestInput instanceof Map)) {
            throw new IllegalArgumentException("Invalid request input format");
        }

        Map<String, Object> map = (Map<String, Object>) requestInput;

        RatingCreateRequest request = new RatingCreateRequest();
        request.setUserId(UUID.fromString((String) map.get("userId")));
        request.setMovieId(UUID.fromString((String) map.get("movieId")));
        request.setRating((Integer) map.get("rating"));
        request.setReview((String) map.get("review"));

        // Handle metadata if present
        if (map.containsKey("metadata") && map.get("metadata") != null) {
            request.setMetadata(convertMetadata((Map<String, Object>) map.get("metadata")));
        }

        return request;
    }

    @SuppressWarnings("unchecked")
    private RatingMetadata convertMetadata(Map<String, Object> metadataMap) {
        RatingMetadata metadata = new RatingMetadata();
        metadata.setIpAddress((String) metadataMap.get("ipAddress"));
        metadata.setUserAgent((String) metadataMap.get("userAgent"));
        metadata.setDeviceFingerprint((String) metadataMap.get("deviceFingerprint"));
        metadata.setSubmissionTime(LocalDateTime.parse((String) metadataMap.get("submissionTime")));
        return metadata;
    }
}
