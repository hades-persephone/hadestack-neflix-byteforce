package io.watch.rating.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FraudDetectionResult {
    private double fraudScore;
    private boolean isFraudulent;
    private boolean requiresReview;
    private List<String> reasons;
}
