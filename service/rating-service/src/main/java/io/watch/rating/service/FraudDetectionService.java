package io.watch.rating.service;

import com.github.dockerjava.zerodep.shaded.org.apache.commons.codec.digest.DigestUtils;
import io.watch.rating.dto.FraudDetectionResult;
import io.watch.rating.dto.RatingCreateRequest;
import io.watch.rating.repository.RatingRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudDetectionService {

    private final RatingRepository ratingRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public FraudDetectionResult detectFraud(@Valid RatingCreateRequest request) {
        double fraudScore = 0.0;
        List<String> reasons = new ArrayList<>();

        if(isRateLimited(request.getUserId(), request.getMetadata().getIpAddress())) {
            fraudScore += 0.4;
            reasons.add("Rate limit reached");
        }

        if(isDuplicateContent(request.getReview())) {
            fraudScore += 0.3;
            reasons.add("Duplicate content detected");
        }

        if(isSuspiciousReview(request.getReview())) {
            fraudScore += 0.2;
            reasons.add("Suspicious review detected");
        }

        if(isSuspiciousIp(request.getMetadata().getIpAddress())) {
            fraudScore += 0.3;
            reasons.add("Suspicious ip detected");
        }

        if(isNewUserWithExtremeRating(request.getUserId(), request.getRating())) {
            fraudScore += 0.2;
            reasons.add("New user with extrem rating detected");
        }

        boolean isFraudulent = fraudScore > 0.7;
        boolean requiresReview = fraudScore > 0.4;

        return FraudDetectionResult.builder()
                .fraudScore(fraudScore)
                .isFraudulent(isFraudulent)
                .requiresReview(requiresReview)
                .reasons(reasons)
                .build();
    }

    private boolean isNewUserWithExtremeRating(UUID userId, Integer rating) {
        Long recentRatings = ratingRepository.countByUserIdAndCreatedAtAfter(userId, LocalDateTime.now().minusDays(7));
        return recentRatings < 3 && (rating == 1 || rating == 5);
    }

    private boolean isSuspiciousIp(String ipAddress) {
        String key = "suspicious_ip:" + ipAddress;
        return Boolean.TRUE.equals(redisTemplate.opsForValue().get(key));
    }

    private boolean isSuspiciousReview(String review) {
        if(review == null) return false;
        String lowerReview = review.toLowerCase();
        String[] spamKeyWords = {"fake", "bot", "spam", "promotional", "advertisement"};
        return Arrays.stream(spamKeyWords).anyMatch(lowerReview::contains);
    }

    private boolean isDuplicateContent(String review) {
        if(review == null || review.trim().isEmpty()) return false;

        String reviewHash = DigestUtils.md5Hex(review).toLowerCase().trim();
        String key = "review_hash:" + reviewHash;

        Boolean exists = redisTemplate.hasKey(key);
        if(!exists) {
            redisTemplate.opsForValue().setIfAbsent(key, review, Duration.ofSeconds(30));
            return false;
        }
        return true;
    }

    private boolean isRateLimited(UUID userId, String ipAddress) {
        String userKey = "rating_limit:user:" + userId;
        String ipKey = "rating_limit:ip:" + ipAddress;

        Long userCount = redisTemplate.opsForValue().increment(userKey);
        Long ipCount = redisTemplate.opsForValue().increment(ipKey);

        if(userCount == 1) {
            redisTemplate.expire(userKey, Duration.ofMinutes(5));
        }
        if(ipCount == 1) {
            redisTemplate.expire(ipKey, Duration.ofMinutes(5));
        }
        return userCount > 10 || ipCount > 50;
    }
}
