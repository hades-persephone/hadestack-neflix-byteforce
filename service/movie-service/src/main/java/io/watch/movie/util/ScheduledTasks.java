package io.watch.movie.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    @Scheduled(fixedRate = 300000)
    public void checkHealth() {
        log.info("Checking health...");
    }

}
