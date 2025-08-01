package io.watch.auth.util;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility class for caching operations.
 * Provides methods for getting and putting values in the cache.
 */
@Component
public class CacheUtils {

    private final CacheManager cacheManager;

    public CacheUtils(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Get a value from the cache.
     *
     * @param cacheName the name of the cache
     * @param key the key to look up
     * @param type the type of the value
     * @param <T> the type of the value
     * @return an Optional containing the value if found, or empty if not found
     */
    public <T> Optional<T> getFromCache(String cacheName, Object key, Class<T> type) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.get(key);
            if (valueWrapper != null && valueWrapper.get() != null) {
                return Optional.of(type.cast(valueWrapper.get()));
            }
        }
        return Optional.empty();
    }

    /**
     * Put a value in the cache.
     *
     * @param cacheName the name of the cache
     * @param key the key to store the value under
     * @param value the value to store
     */
    public void putInCache(String cacheName, Object key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }

    /**
     * Evict a value from the cache.
     *
     * @param cacheName the name of the cache
     * @param key the key to evict
     */
    public void evictFromCache(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    /**
     * Clear the entire cache.
     *
     * @param cacheName the name of the cache
     */
    public void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
