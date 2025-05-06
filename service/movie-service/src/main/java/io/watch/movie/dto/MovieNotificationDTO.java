package io.watch.movie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieNotificationDTO {
    private String type;
    private String message;
}
