package io.watch.rating.exception;

import lombok.Getter;

@Getter
public class FraudDetectionException extends RatingServiceException {
    private final String fraudReason;
    private final double fraudScore;

    public FraudDetectionException(String message, String fraudReason, double fraudScore) {
        super(message, "FRAUD_DETECTION_ERROR");
        this.fraudReason = fraudReason;
        this.fraudScore = fraudScore;
    }

    public FraudDetectionException(String message, String fraudReason, double fraudScore, Throwable cause) {
        super(message, "FRAUD_DETECTION_ERROR", cause);
        this.fraudReason = fraudReason;
        this.fraudScore = fraudScore;
    }
}