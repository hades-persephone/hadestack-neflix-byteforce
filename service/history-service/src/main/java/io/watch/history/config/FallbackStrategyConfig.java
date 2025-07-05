package io.watch.history.config;

import io.watch.history.handler.fallbackstrategy.FallbackStorageStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;

@Configuration
public class FallbackStrategyConfig {
    @Bean
    public List<FallbackStorageStrategy> fallbackStorageStrategies(List<FallbackStorageStrategy> fallbackStorageStrategies) {
        return fallbackStorageStrategies
                .stream()
                .sorted(Comparator.comparingInt(FallbackStorageStrategy::getPriority))
                .toList();
    }
}
