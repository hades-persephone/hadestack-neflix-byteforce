package io.watch.history.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.error("Cache get error for key [{}] in cache [{}]: {}", key, cache.getName(), exception.getMessage());
        cache.evict(key);
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.error("Cache put error for key [{}] in cache [{}]: {}", key, cache.getName(), exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.error("Cache evict error for key [{}] in cache [{}]: {}", key, cache.getName(), exception.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.error("Cache clear error in cache [{}]: {}", cache.getName(), exception.getMessage());
    }
}
