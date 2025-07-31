package co.tyrell.omdb_mcp_server.service;

import co.tyrell.omdb_mcp_server.config.McpProperties;
import co.tyrell.omdb_mcp_server.model.mcp.McpRequest;
import co.tyrell.omdb_mcp_server.model.mcp.McpResponse;
import co.tyrell.omdb_mcp_server.model.mcp.McpTool;
import co.tyrell.omdb_mcp_server.model.omdb.OmdbMovie;
import co.tyrell.omdb_mcp_server.model.omdb.OmdbSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementing MCP protocol for OMDB operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class McpService {
    
    private final OmdbService omdbService;
    private final McpProperties mcpProperties;
    
    /**
     * Handle MCP requests
     */
    public Mono<McpResponse> handleRequest(McpRequest request) {
        log.debug("Handling MCP request: {}", request.getMethod());
        
        McpResponse response = new McpResponse();
        response.setId(request.getId());
        
        return switch (request.getMethod()) {
            case "initialize" -> handleInitialize(request, response);
            case "tools/list" -> handleToolsList(request, response);
            case "tools/call" -> handleToolCall(request, response);
            default -> {
                response.setError(createError(-32601, "Method not found", null));
                yield Mono.just(response);
            }
        };
    }
    
    private Mono<McpResponse> handleInitialize(McpRequest request, McpResponse response) {
        Map<String, Object> result = new HashMap<>();
        result.put("protocolVersion", "2024-11-05");
        result.put("capabilities", Map.of("tools", Map.of()));
        result.put("serverInfo", Map.of(
                "name", mcpProperties.getName(),
                "version", mcpProperties.getVersion()
        ));
        
        response.setResult(result);
        return Mono.just(response);
    }
    
    private Mono<McpResponse> handleToolsList(McpRequest request, McpResponse response) {
        List<McpTool> tools = Arrays.asList(
                createSearchMoviesTool(),
                createGetMovieDetailsTool(),
                createGetMovieByImdbIdTool()
        );
        
        response.setResult(Map.of("tools", tools));
        return Mono.just(response);
    }
    
    private Mono<McpResponse> handleToolCall(McpRequest request, McpResponse response) {
        Map<String, Object> params = request.getParams();
        String toolName = (String) params.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
        
        return switch (toolName) {
            case "search_movies" -> handleSearchMovies(arguments, response);
            case "get_movie_details" -> handleGetMovieDetails(arguments, response);
            case "get_movie_by_imdb_id" -> handleGetMovieByImdbId(arguments, response);
            default -> {
                response.setError(createError(-32602, "Invalid tool name", null));
                yield Mono.just(response);
            }
        };
    }
    
    private Mono<McpResponse> handleSearchMovies(Map<String, Object> arguments, McpResponse response) {
        String title = (String) arguments.get("title");
        String year = (String) arguments.get("year");
        String type = (String) arguments.get("type");
        
        if (title == null || title.trim().isEmpty()) {
            response.setError(createError(-32602, "Title parameter is required", null));
            return Mono.just(response);
        }
        
        return omdbService.searchMovies(title, year, type)
                .map(searchResponse -> {
                    if ("True".equals(searchResponse.getResponse())) {
                        response.setResult(Map.of(
                                "content", List.of(Map.of(
                                        "type", "text",
                                        "text", formatSearchResults(searchResponse)
                                ))
                        ));
                    } else {
                        response.setResult(Map.of(
                                "content", List.of(Map.of(
                                        "type", "text",
                                        "text", "No movies found: " + (searchResponse.getError() != null ? searchResponse.getError() : "Unknown error")
                                ))
                        ));
                    }
                    return response;
                })
                .onErrorResume(error -> {
                    log.error("Error searching movies", error);
                    response.setError(createError(-32603, "Internal error: " + error.getMessage(), null));
                    return Mono.just(response);
                });
    }
    
    private Mono<McpResponse> handleGetMovieDetails(Map<String, Object> arguments, McpResponse response) {
        String title = (String) arguments.get("title");
        String year = (String) arguments.get("year");
        String plot = (String) arguments.get("plot");
        
        if (title == null || title.trim().isEmpty()) {
            response.setError(createError(-32602, "Title parameter is required", null));
            return Mono.just(response);
        }
        
        return omdbService.getMovieByTitle(title, year, plot)
                .map(movie -> {
                    if ("True".equals(movie.getResponse())) {
                        response.setResult(Map.of(
                                "content", List.of(Map.of(
                                        "type", "text",
                                        "text", formatMovieDetails(movie)
                                ))
                        ));
                    } else {
                        response.setResult(Map.of(
                                "content", List.of(Map.of(
                                        "type", "text",
                                        "text", "Movie not found: " + (movie.getError() != null ? movie.getError() : "Unknown error")
                                ))
                        ));
                    }
                    return response;
                })
                .onErrorResume(error -> {
                    log.error("Error getting movie details", error);
                    response.setError(createError(-32603, "Internal error: " + error.getMessage(), null));
                    return Mono.just(response);
                });
    }
    
    private Mono<McpResponse> handleGetMovieByImdbId(Map<String, Object> arguments, McpResponse response) {
        String imdbId = (String) arguments.get("imdbId");
        String plot = (String) arguments.get("plot");
        
        if (imdbId == null || imdbId.trim().isEmpty()) {
            response.setError(createError(-32602, "imdbId parameter is required", null));
            return Mono.just(response);
        }
        
        return omdbService.getMovieByImdbId(imdbId, plot)
                .map(movie -> {
                    if ("True".equals(movie.getResponse())) {
                        response.setResult(Map.of(
                                "content", List.of(Map.of(
                                        "type", "text",
                                        "text", formatMovieDetails(movie)
                                ))
                        ));
                    } else {
                        response.setResult(Map.of(
                                "content", List.of(Map.of(
                                        "type", "text",
                                        "text", "Movie not found: " + (movie.getError() != null ? movie.getError() : "Unknown error")
                                ))
                        ));
                    }
                    return response;
                })
                .onErrorResume(error -> {
                    log.error("Error getting movie by IMDB ID", error);
                    response.setError(createError(-32603, "Internal error: " + error.getMessage(), null));
                    return Mono.just(response);
                });
    }
    
    private String formatSearchResults(OmdbSearchResponse searchResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append("Search Results (").append(searchResponse.getTotalResults()).append(" total):\n\n");
        
        for (OmdbSearchResponse.SearchResult result : searchResponse.getSearch()) {
            sb.append("• ").append(result.getTitle()).append(" (").append(result.getYear()).append(")\n");
            sb.append("  Type: ").append(result.getType()).append("\n");
            sb.append("  IMDB ID: ").append(result.getImdbId()).append("\n\n");
        }
        
        return sb.toString();
    }
    
    private String formatMovieDetails(OmdbMovie movie) {
        StringBuilder sb = new StringBuilder();
        sb.append("🎬 ").append(movie.getTitle()).append(" (").append(movie.getYear()).append(")\n\n");
        
        if (movie.getRated() != null) sb.append("Rating: ").append(movie.getRated()).append("\n");
        if (movie.getRuntime() != null) sb.append("Runtime: ").append(movie.getRuntime()).append("\n");
        if (movie.getGenre() != null) sb.append("Genre: ").append(movie.getGenre()).append("\n");
        if (movie.getDirector() != null) sb.append("Director: ").append(movie.getDirector()).append("\n");
        if (movie.getActors() != null) sb.append("Cast: ").append(movie.getActors()).append("\n");
        if (movie.getImdbRating() != null) sb.append("IMDB Rating: ").append(movie.getImdbRating()).append("/10\n");
        if (movie.getMetascore() != null) sb.append("Metacritic Score: ").append(movie.getMetascore()).append("/100\n");
        
        sb.append("\nPlot:\n").append(movie.getPlot() != null ? movie.getPlot() : "No plot available");
        
        if (movie.getAwards() != null) {
            sb.append("\n\nAwards: ").append(movie.getAwards());
        }
        
        return sb.toString();
    }
    
    private McpTool createSearchMoviesTool() {
        McpTool tool = new McpTool();
        tool.setName("search_movies");
        tool.setDescription("Search for movies by title in the OMDB database");
        
        McpTool.InputSchema schema = new McpTool.InputSchema();
        McpTool.InputSchema.Properties properties = new McpTool.InputSchema.Properties();
        
        McpTool.InputSchema.Properties.Property titleProp = new McpTool.InputSchema.Properties.Property();
        titleProp.setType("string");
        titleProp.setDescription("Movie title to search for");
        properties.setTitle(titleProp);
        
        McpTool.InputSchema.Properties.Property yearProp = new McpTool.InputSchema.Properties.Property();
        yearProp.setType("string");
        yearProp.setDescription("Year of release (optional)");
        properties.setYear(yearProp);
        
        McpTool.InputSchema.Properties.Property typeProp = new McpTool.InputSchema.Properties.Property();
        typeProp.setType("string");
        typeProp.setDescription("Type of result: movie, series, or episode (optional)");
        properties.setType(typeProp);
        
        schema.setProperties(properties);
        schema.setRequired(List.of("title"));
        tool.setInputSchema(schema);
        
        return tool;
    }
    
    private McpTool createGetMovieDetailsTool() {
        McpTool tool = new McpTool();
        tool.setName("get_movie_details");
        tool.setDescription("Get detailed information about a specific movie by title");
        
        McpTool.InputSchema schema = new McpTool.InputSchema();
        McpTool.InputSchema.Properties properties = new McpTool.InputSchema.Properties();
        
        McpTool.InputSchema.Properties.Property titleProp = new McpTool.InputSchema.Properties.Property();
        titleProp.setType("string");
        titleProp.setDescription("Movie title");
        properties.setTitle(titleProp);
        
        McpTool.InputSchema.Properties.Property yearProp = new McpTool.InputSchema.Properties.Property();
        yearProp.setType("string");
        yearProp.setDescription("Year of release (optional)");
        properties.setYear(yearProp);
        
        McpTool.InputSchema.Properties.Property plotProp = new McpTool.InputSchema.Properties.Property();
        plotProp.setType("string");
        plotProp.setDescription("Plot length: short or full (optional, default: full)");
        properties.setPlot(plotProp);
        
        schema.setProperties(properties);
        schema.setRequired(List.of("title"));
        tool.setInputSchema(schema);
        
        return tool;
    }
    
    private McpTool createGetMovieByImdbIdTool() {
        McpTool tool = new McpTool();
        tool.setName("get_movie_by_imdb_id");
        tool.setDescription("Get detailed information about a movie by IMDB ID");
        
        McpTool.InputSchema schema = new McpTool.InputSchema();
        McpTool.InputSchema.Properties properties = new McpTool.InputSchema.Properties();
        
        McpTool.InputSchema.Properties.Property imdbIdProp = new McpTool.InputSchema.Properties.Property();
        imdbIdProp.setType("string");
        imdbIdProp.setDescription("IMDB ID (e.g., tt0111161)");
        properties.setImdbId(imdbIdProp);
        
        McpTool.InputSchema.Properties.Property plotProp = new McpTool.InputSchema.Properties.Property();
        plotProp.setType("string");
        plotProp.setDescription("Plot length: short or full (optional, default: full)");
        properties.setPlot(plotProp);
        
        schema.setProperties(properties);
        schema.setRequired(List.of("imdbId"));
        tool.setInputSchema(schema);
        
        return tool;
    }
    
    private McpResponse.McpError createError(int code, String message, Object data) {
        McpResponse.McpError error = new McpResponse.McpError();
        error.setCode(code);
        error.setMessage(message);
        error.setData(data);
        return error;
    }
}
