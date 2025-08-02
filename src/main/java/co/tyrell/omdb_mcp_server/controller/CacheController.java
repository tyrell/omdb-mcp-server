package co.tyrell.omdb_mcp_server.controller;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for cache management and statistics
 */
@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
@Slf4j
public class CacheController {
    
    private final CacheManager cacheManager;
    
    /**
     * Get cache statistics for all caches
     */
    @GetMapping("/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        for (String cacheName : cacheManager.getCacheNames()) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache) {
                CaffeineCache caffeineCache = (CaffeineCache) cache;
                Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
                
                Map<String, Object> cacheStats = new HashMap<>();
                cacheStats.put("size", nativeCache.estimatedSize());
                cacheStats.put("hitCount", nativeCache.stats().hitCount());
                cacheStats.put("missCount", nativeCache.stats().missCount());
                cacheStats.put("hitRate", nativeCache.stats().hitRate());
                cacheStats.put("missRate", nativeCache.stats().missRate());
                cacheStats.put("requestCount", nativeCache.stats().requestCount());
                cacheStats.put("loadCount", nativeCache.stats().loadCount());
                cacheStats.put("evictionCount", nativeCache.stats().evictionCount());
                
                stats.put(cacheName, cacheStats);
            }
        }
        
        return stats;
    }
    
    /**
     * Clear all caches
     */
    @DeleteMapping("/clear")
    public Map<String, String> clearAllCaches() {
        for (String cacheName : cacheManager.getCacheNames()) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("Cleared cache: {}", cacheName);
            }
        }
        return Map.of("message", "All caches cleared successfully");
    }
    
    /**
     * Clear specific cache
     */
    @DeleteMapping("/clear/{cacheName}")
    public Map<String, String> clearCache(@PathVariable String cacheName) {
        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Cleared cache: {}", cacheName);
            return Map.of("message", "Cache '" + cacheName + "' cleared successfully");
        } else {
            return Map.of("error", "Cache '" + cacheName + "' not found");
        }
    }
}
