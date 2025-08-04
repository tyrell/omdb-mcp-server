package co.tyrell.omdb_mcp_server.config;

import co.tyrell.omdb_mcp_server.service.MovieSearchTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Function;

/**
 * Configuration for Spring AI MCP Server integration
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class McpServerConfig {
    
    private final MovieSearchTools movieSearchTools;
    
    /**
     * Configure search movies tool function
     */
    @Bean
    public Function<MovieSearchTools.SearchMoviesRequest, String> searchMoviesFunction() {
        log.info("Registering searchMovies function for Spring AI MCP server");
        return request -> movieSearchTools.searchMovies(request.title(), request.year(), request.type());
    }
    
    /**
     * Configure get movie details tool function
     */
    @Bean
    public Function<MovieSearchTools.MovieDetailsRequest, String> getMovieDetailsFunction() {
        log.info("Registering getMovieDetails function for Spring AI MCP server");
        return request -> movieSearchTools.getMovieDetails(request.title(), request.year(), request.plot());
    }
    
    /**
     * Configure get movie by IMDB ID tool function
     */
    @Bean
    public Function<MovieSearchTools.MovieByImdbIdRequest, String> getMovieByImdbIdFunction() {
        log.info("Registering getMovieByImdbId function for Spring AI MCP server");
        return request -> movieSearchTools.getMovieByImdbId(request.imdbId(), request.plot());
    }
}
