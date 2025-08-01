package io.watch.auth.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Configuration class for caching.
 * Sets up the caches used by the application.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Create a cache manager with the required caches.
     *
     * @return the cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache("permissionMap"),
                new ConcurrentMapCache("publicEndpoints"),
                new ConcurrentMapCache("endpointPermissions"),
                new ConcurrentMapCache("contextualRequirements")
        ));
        return cacheManager;
    }
}