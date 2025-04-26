package io.watch.search.service.kafka;

import io.watch.search.model.entity.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MovieConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MovieConsumer.class);
    private static final String MOVIES_TOPIC = "movies";

    private final ElasticsearchOperations elasticsearchOperations;

    public MovieConsumer(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @KafkaListener(
            topics = MOVIES_TOPIC,
            groupId = "search-movies",
            containerFactory = "movieKafkaListenerContainerFactory"
    )
    public void consumeMovie(Movie movie) {
        try {
            elasticsearchOperations.save(movie);
            logger.info("Indexed movie in Elasticsearch: movieId={}, title={}", movie.getMovieId(), movie.getTitle());
        } catch (Exception e) {
            logger.error("Failed to index movie: movieId={}, error={}", movie.getMovieId(), e.getMessage());
            throw new RuntimeException("Failed to index movie", e); // For DLQ
        }
    }
}