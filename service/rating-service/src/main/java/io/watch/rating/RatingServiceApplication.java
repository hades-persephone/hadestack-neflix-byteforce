package io.watch.rating;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
public class RatingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RatingServiceApplication.class, args);
    }

}
