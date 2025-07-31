package co.tyrell.omdb_mcp_server.service;

import co.tyrell.omdb_mcp_server.config.OmdbProperties;
import co.tyrell.omdb_mcp_server.model.omdb.OmdbMovie;
import co.tyrell.omdb_mcp_server.model.omdb.OmdbSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Service for interacting with OMDB API
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OmdbService {
    
    private final WebClient webClient;
    private final OmdbProperties omdbProperties;
    
    /**
     * Search for movies by title
     */
    public Mono<OmdbSearchResponse> searchMovies(String title, String year, String type) {
        log.debug("Searching movies with title: {}, year: {}, type: {}", title, year, type);
        
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.scheme("https")
                            .host("www.omdbapi.com")
                            .path("/")
                            .queryParam("apikey", omdbProperties.getKey())
                            .queryParam("s", title);
                    
                    if (StringUtils.hasText(year)) {
                        uriBuilder.queryParam("y", year);
                    }
                    
                    if (StringUtils.hasText(type)) {
                        uriBuilder.queryParam("type", type);
                    }
                    
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(OmdbSearchResponse.class)
                .doOnNext(response -> log.debug("Received search response: {}", response))
                .doOnError(error -> log.error("Error searching movies: {}", error.getMessage(), error));
    }
    
    /**
     * Get movie details by title
     */
    public Mono<OmdbMovie> getMovieByTitle(String title, String year, String plot) {
        log.debug("Getting movie by title: {}, year: {}, plot: {}", title, year, plot);
        
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.scheme("https")
                            .host("www.omdbapi.com")
                            .path("/")
                            .queryParam("apikey", omdbProperties.getKey())
                            .queryParam("t", title);
                    
                    if (StringUtils.hasText(year)) {
                        uriBuilder.queryParam("y", year);
                    }
                    
                    if (StringUtils.hasText(plot)) {
                        uriBuilder.queryParam("plot", plot);
                    } else {
                        uriBuilder.queryParam("plot", "full");
                    }
                    
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(OmdbMovie.class)
                .doOnNext(movie -> log.debug("Received movie details: {}", movie.getTitle()))
                .doOnError(error -> log.error("Error getting movie by title: {}", error.getMessage(), error));
    }
    
    /**
     * Get movie details by IMDB ID
     */
    public Mono<OmdbMovie> getMovieByImdbId(String imdbId, String plot) {
        log.debug("Getting movie by IMDB ID: {}, plot: {}", imdbId, plot);
        
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.scheme("https")
                            .host("www.omdbapi.com")
                            .path("/")
                            .queryParam("apikey", omdbProperties.getKey())
                            .queryParam("i", imdbId);
                    
                    if (StringUtils.hasText(plot)) {
                        uriBuilder.queryParam("plot", plot);
                    } else {
                        uriBuilder.queryParam("plot", "full");
                    }
                    
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(OmdbMovie.class)
                .doOnNext(movie -> log.debug("Received movie details: {}", movie.getTitle()))
                .doOnError(error -> log.error("Error getting movie by IMDB ID: {}", error.getMessage(), error));
    }
}
