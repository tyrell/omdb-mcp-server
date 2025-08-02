# OMDB MCP Server - Caching Implementation

## Overview

The OMDB MCP Server now includes intelligent caching to reduce unnecessary API calls to the OMDB API, helping you stay within rate limits and improve response times.

## Features

### ✨ Intelligent Caching
- **Automatic Caching**: All OMDB API responses are automatically cached
- **Configurable TTL**: Cache entries expire after 1 hour by default
- **Memory Efficient**: Maximum cache size of 1000 entries with LRU eviction
- **Statistics Tracking**: Built-in cache performance monitoring

### 🗂️ Cache Types
- **Movie Search Cache**: Caches search results by title, year, and type
- **Movie Details Cache**: Caches detailed movie information by title
- **IMDB ID Cache**: Caches movie details retrieved by IMDB ID

### 🔧 Cache Configuration

Cache behavior can be configured via application properties:

```properties
# Cache Configuration
cache.expire-after-write=1h        # Cache TTL (Time To Live)
cache.maximum-size=1000           # Maximum number of cached entries
cache.record-stats=true           # Enable cache statistics
```

### 📊 Cache Management Endpoints

The server provides REST endpoints for cache management:

#### Get Cache Statistics
```http
GET /cache/stats
```

Returns detailed statistics for all caches including:
- Cache size
- Hit/miss counts and rates
- Request counts
- Eviction counts

Example response:
```json
{
  "movieSearch": {
    "size": 45,
    "hitCount": 127,
    "missCount": 23,
    "hitRate": 0.8467,
    "missRate": 0.1533,
    "requestCount": 150,
    "loadCount": 23,
    "evictionCount": 0
  },
  "movieByTitle": {
    "size": 32,
    "hitCount": 89,
    "missCount": 15,
    "hitRate": 0.8558,
    "missRate": 0.1442,
    "requestCount": 104,
    "loadCount": 15,
    "evictionCount": 0
  }
}
```

#### Clear All Caches
```http
DELETE /cache/clear
```

#### Clear Specific Cache
```http
DELETE /cache/clear/{cacheName}
```

Valid cache names:
- `movieSearch`
- `movieByTitle`
- `movieByImdbId`

### 🚀 Performance Benefits

With caching enabled, you can expect:

1. **Reduced API Calls**: Identical requests are served from cache
2. **Faster Response Times**: Cached responses are served instantly
3. **Rate Limit Protection**: Fewer API calls help stay within OMDB API limits
4. **Cost Savings**: Reduced API usage if using a paid OMDB plan

### 🎯 Cache Key Strategy

Cache keys are carefully constructed to ensure proper cache hit/miss behavior:

- **Movie Search**: `title_year_type` (e.g., "Inception_2010_movie")
- **Movie by Title**: `title_year_plot` (e.g., "Inception_2010_full")
- **Movie by IMDB ID**: `imdbId_plot` (e.g., "tt1375666_full")

### 🔍 Monitoring Cache Performance

Monitor cache effectiveness using the statistics endpoint:

- **High Hit Rate** (>80%): Good cache performance
- **Low Hit Rate** (<50%): Consider adjusting cache configuration
- **High Eviction Count**: Consider increasing maximum cache size

### ⚙️ Technical Implementation

- **Cache Provider**: Caffeine (high-performance Java caching library)
- **Cache Type**: In-memory with TTL-based expiration
- **Thread Safety**: Fully thread-safe for concurrent access
- **Spring Integration**: Uses Spring Cache abstraction with `@Cacheable` annotations

### 🧪 Testing

The caching implementation includes comprehensive tests:

- Cache configuration validation
- Cache key generation testing
- Cache statistics verification
- Cache management endpoint testing

Run tests with:
```bash
mvn test -Dtest=*Cache*
```

### 🔄 Cache Lifecycle

1. **Request Received**: Check if response exists in cache
2. **Cache Hit**: Return cached response immediately
3. **Cache Miss**: Call OMDB API, cache response, return to client
4. **Expiration**: Remove expired entries automatically
5. **Eviction**: Remove least recently used entries when cache is full

This intelligent caching system ensures optimal performance while maintaining data freshness and respecting API rate limits.
