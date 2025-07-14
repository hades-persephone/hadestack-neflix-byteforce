package io.watch.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingMetadata {
    private String ipAddress;
    private String userAgent;
    private String deviceFingerprint;
    private LocalDateTime submissionTime;
}
