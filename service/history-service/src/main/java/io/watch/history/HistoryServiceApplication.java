package io.watch.history;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HistoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HistoryServiceApplication.class, args);
    }

}
