package co.tyrell.omdb_mcp_server.controller;

import co.tyrell.omdb_mcp_server.config.CacheConfig;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CacheController.class)
class CacheControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CacheManager cacheManager;

    @Test
    void testGetCacheStats() throws Exception {
        // Create a real Caffeine cache with stats enabled for testing
        Cache<Object, Object> nativeCache = Caffeine.newBuilder()
                .recordStats()
                .build();
        
        // Create a mock CaffeineCache that returns our real native cache
        CaffeineCache mockCaffeineCache = mock(CaffeineCache.class);
        when(mockCaffeineCache.getNativeCache()).thenReturn(nativeCache);
        
        when(cacheManager.getCacheNames()).thenReturn(Set.of(CacheConfig.MOVIE_SEARCH_CACHE));
        when(cacheManager.getCache(CacheConfig.MOVIE_SEARCH_CACHE)).thenReturn(mockCaffeineCache);

        mockMvc.perform(get("/cache/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testClearAllCaches() throws Exception {
        // Create a mock CaffeineCache
        CaffeineCache mockCaffeineCache = mock(CaffeineCache.class);
        
        when(cacheManager.getCacheNames()).thenReturn(Set.of(CacheConfig.MOVIE_SEARCH_CACHE));
        when(cacheManager.getCache(CacheConfig.MOVIE_SEARCH_CACHE)).thenReturn(mockCaffeineCache);

        mockMvc.perform(delete("/cache/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All caches cleared successfully"));
    }

    @Test
    void testClearSpecificCache() throws Exception {
        // Create a mock CaffeineCache
        CaffeineCache mockCaffeineCache = mock(CaffeineCache.class);
        
        when(cacheManager.getCache(CacheConfig.MOVIE_SEARCH_CACHE)).thenReturn(mockCaffeineCache);

        mockMvc.perform(delete("/cache/clear/" + CacheConfig.MOVIE_SEARCH_CACHE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cache '" + CacheConfig.MOVIE_SEARCH_CACHE + "' cleared successfully"));
    }

    @Test
    void testClearNonExistentCache() throws Exception {
        when(cacheManager.getCache("nonexistent")).thenReturn(null);

        mockMvc.perform(delete("/cache/clear/nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Cache 'nonexistent' not found"));
    }
}
