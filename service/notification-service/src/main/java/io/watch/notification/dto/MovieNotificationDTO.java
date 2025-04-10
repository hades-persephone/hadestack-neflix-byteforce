package io.watch.notification.dto;

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
