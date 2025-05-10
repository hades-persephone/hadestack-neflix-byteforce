package io.watch.movie.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public <T> void put(String key, T value) {
        try {
            if (key == null || value == null) {
                log.warn("Skipping cache put due to null key or value: key={}", key);
                return;
            }
            redisTemplate.opsForValue().set(key, value);
            log.debug("Added object to cache with key: {}", key);
        } catch (Exception e) {
            log.error("Error putting object in cache with key: {}", key, e);
        }
    }

    public <T> void put(String key, T value, Duration ttl) {
        try {
            if (key == null || value == null || ttl == null) {
                log.warn("Skipping cache put due to null key, value, or TTL: key={}", key);
                return;
            }
            if (ttl.isNegative() || ttl.isZero()) {
                log.warn("Skipping cache put due to invalid TTL: key={}, ttl={}", key, ttl);
                return;
            }
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("Added object to cache with key: {} and TTL: {}", key, ttl);
        } catch (Exception e) {
            log.error("Error putting object in cache with key: {}", key, e);
        }
    }

    public <T> Optional<T> get(String key, Class<T> clazz) {
        try {
            if (key == null || clazz == null) {
                log.warn("Skipping cache get due to null key or class: key={}", key);
                return Optional.empty();
            }
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                log.debug("Cache miss for key: {}", key);
                return Optional.empty();
            }
            return Optional.of(objectMapper.convertValue(value, clazz));
        } catch (Exception e) {
            log.error("Error getting object from cache with key: {}", key, e);
            return Optional.empty();
        }
    }

    public void evict(String key) {
        try {
            if (key == null) {
                log.warn("Skipping cache evict due to null key");
                return;
            }
            Boolean deleted = redisTemplate.delete(key);
            if (deleted) {
                log.debug("Evicted cache entry with key: {}", key);
            } else {
                log.debug("No cache entry found to evict for key: {}", key);
            }
        } catch (Exception e) {
            log.error("Error evicting cache entry with key: {}", key, e);
        }
    }

    public void evictPattern(String pattern) {
        try {
            if (pattern == null || pattern.trim().isEmpty()) {
                log.warn("Skipping cache evict due to null or empty pattern");
                return;
            }
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long deletedCount = redisTemplate.delete(keys);
                log.debug("Evicted {} cache entries matching pattern: {}", deletedCount, pattern);
            } else {
                log.debug("No cache entries found matching pattern: {}", pattern);
            }
        } catch (Exception e) {
            log.error("Error evicting cache entries with pattern: {}", pattern, e);
        }
    }
}