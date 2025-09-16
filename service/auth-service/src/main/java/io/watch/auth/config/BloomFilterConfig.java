package io.watch.auth.config;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
public class BloomFilterConfig {

    @Bean
    @SuppressWarnings("UnstableApiUsage")
    public BloomFilter<String> usernameBloomFilter() {
        return BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 10000, 0.01);
    }

    @Bean
    @SuppressWarnings("UnstableApiUsage")
    public BloomFilter<String> emailBloomFilter() {
        return BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 10000, 0.01);
    }

}
