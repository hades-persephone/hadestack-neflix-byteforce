package io.watch.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieValidationResponse {
    private UUID movieId;
    private String title;
    private boolean isValid;
    private String errorMessage;
}
