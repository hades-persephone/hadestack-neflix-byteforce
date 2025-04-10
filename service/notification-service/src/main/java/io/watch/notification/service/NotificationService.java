package io.watch.notification.service;


import io.watch.notification.dto.MovieNotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    public void handleNotification(MovieNotificationDTO dto) {
        switch (dto.getType()) {
            case "NEW_MOVIE" -> handleNewMovie(dto.getMessage());
            case "MOVIE_UPDATED" -> handleUpdatedMovie(dto.getMessage());
            default -> log.warn("⚠️ Unknown notification type: {}", dto.getType());
        }
    }

    private void handleNewMovie(String message) {
        // Xử lý như gửi email, push, log...
        log.info("🎬 [NEW MOVIE] {}", message);
    }

    private void handleUpdatedMovie(String message) {
        log.info("🔄 [MOVIE UPDATED] {}", message);
    }
}
