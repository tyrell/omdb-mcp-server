package co.tyrell.omdb_mcp_server.service;

import co.tyrell.omdb_mcp_server.config.McpProperties;
import co.tyrell.omdb_mcp_server.model.mcp.McpRequest;
import co.tyrell.omdb_mcp_server.model.mcp.McpResponse;
import co.tyrell.omdb_mcp_server.model.mcp.McpTool;
import co.tyrell.omdb_mcp_server.model.omdb.OmdbMovie;
import co.tyrell.omdb_mcp_server.model.omdb.OmdbSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class McpServiceTest {

    @Mock
    private OmdbService omdbService;

    @Mock
    private McpProperties mcpProperties;

    private McpService mcpService;

    @BeforeEach
    void setUp() {
        when(mcpProperties.getName()).thenReturn("OMDB MCP Server");
        when(mcpProperties.getVersion()).thenReturn("0.2.0");
        when(mcpProperties.getDescription()).thenReturn("MCP server for OMDB movie database");
        
        mcpService = new McpService(omdbService, mcpProperties);
    }

    @Test
    void handleInitializeRequest_ShouldReturnValidResponse() {
        // Given
        McpRequest request = new McpRequest();
        request.setId("test-id");
        request.setMethod("initialize");
        
        Map<String, Object> params = new HashMap<>();
        params.put("protocolVersion", "2024-11-05");
        
        Map<String, Object> clientInfo = new HashMap<>();
        clientInfo.put("name", "TestClient");
        clientInfo.put("version", "1.0.0");
        params.put("clientInfo", clientInfo);
        
        request.setParams(params);

        // When
        StepVerifier.create(mcpService.handleRequest(request))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo("test-id");
                    assertThat(response.getError()).isNull();
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) response.getResult();
                    assertThat(result.get("protocolVersion")).isEqualTo("2024-11-05");
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> capabilities = (Map<String, Object>) result.get("capabilities");
                    assertThat(capabilities).isNotNull();
                    assertThat(capabilities).containsKey("tools");
                    assertThat(capabilities).containsKey("logging");
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> serverInfo = (Map<String, Object>) result.get("serverInfo");
                    assertThat(serverInfo.get("name")).isEqualTo("OMDB MCP Server");
                    assertThat(serverInfo.get("version")).isEqualTo("0.2.0");
                    assertThat(serverInfo.get("description")).isEqualTo("MCP server for OMDB movie database");
                })
                .verifyComplete();
    }

    @Test
    void handlePingRequest_ShouldReturnPong() {
        // Given
        McpRequest request = new McpRequest();
        request.setId("ping-id");
        request.setMethod("ping");

        // When
        StepVerifier.create(mcpService.handleRequest(request))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo("ping-id");
                    assertThat(response.getError()).isNull();
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) response.getResult();
                    assertThat(result.get("pong")).isEqualTo(true);
                })
                .verifyComplete();
    }

    @Test
    void handleInitializedNotification_ShouldReturnEmpty() {
        // Given
        McpRequest request = new McpRequest();
        request.setId("init-id");
        request.setMethod("notifications/initialized");

        // When
        StepVerifier.create(mcpService.handleRequest(request))
                .verifyComplete();
    }

    @Test
    void handleToolsListRequest_ShouldReturnAllTools() {
        // Given
        McpRequest request = new McpRequest();
        request.setId("tools-id");
        request.setMethod("tools/list");

        // When
        StepVerifier.create(mcpService.handleRequest(request))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo("tools-id");
                    assertThat(response.getError()).isNull();
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) response.getResult();
                    
                    @SuppressWarnings("unchecked")
                    List<McpTool> tools = (List<McpTool>) result.get("tools");
                    assertThat(tools).hasSize(3);
                    
                    // Verify tool names
                    List<String> toolNames = tools.stream().map(McpTool::getName).toList();
                    assertThat(toolNames).containsExactlyInAnyOrder(
                            "search_movies", "get_movie_details", "get_movie_by_imdb_id");
                    
                    // Verify search_movies tool schema
                    McpTool searchTool = tools.stream()
                            .filter(tool -> "search_movies".equals(tool.getName()))
                            .findFirst()
                            .orElseThrow();
                    
                    assertThat(searchTool.getDescription()).contains("Search for movies");
                    assertThat(searchTool.getInputSchema()).isNotNull();
                    assertThat(searchTool.getInputSchema().getRequired()).contains("title");
                    assertThat(searchTool.getInputSchema().getProperties()).containsKey("title");
                    assertThat(searchTool.getInputSchema().getProperties()).containsKey("year");
                    assertThat(searchTool.getInputSchema().getProperties()).containsKey("type");
                })
                .verifyComplete();
    }

    @Test
    void handleSearchMoviesToolCall_WithValidData_ShouldReturnResults() {
        // Given
        OmdbSearchResponse.SearchResult searchResult = new OmdbSearchResponse.SearchResult();
        searchResult.setTitle("The Matrix");
        searchResult.setYear("1999");
        searchResult.setType("movie");
        searchResult.setImdbId("tt0133093");
        
        OmdbSearchResponse searchResponse = new OmdbSearchResponse();
        searchResponse.setSearch(new OmdbSearchResponse.SearchResult[]{searchResult});
        searchResponse.setTotalResults("1");
        searchResponse.setResponse("True");
        
        when(omdbService.searchMovies("The Matrix", null, null))
                .thenReturn(Mono.just(searchResponse));

        McpRequest request = new McpRequest();
        request.setId("search-id");
        request.setMethod("tools/call");
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", "search_movies");
        
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("title", "The Matrix");
        params.put("arguments", arguments);
        
        request.setParams(params);

        // When
        StepVerifier.create(mcpService.handleRequest(request))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo("search-id");
                    assertThat(response.getError()).isNull();
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) response.getResult();
                    
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
                    assertThat(content).hasSize(1);
                    
                    Map<String, Object> textContent = content.get(0);
                    assertThat(textContent.get("type")).isEqualTo("text");
                    
                    String text = (String) textContent.get("text");
                    assertThat(text).contains("The Matrix");
                    assertThat(text).contains("1999");
                    assertThat(text).contains("tt0133093");
                })
                .verifyComplete();
    }

    @Test
    void handleSearchMoviesToolCall_WithMissingTitle_ShouldReturnError() {
        // Given
        McpRequest request = new McpRequest();
        request.setId("search-error-id");
        request.setMethod("tools/call");
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", "search_movies");
        
        Map<String, Object> arguments = new HashMap<>();
        // No title provided
        params.put("arguments", arguments);
        
        request.setParams(params);

        // When
        StepVerifier.create(mcpService.handleRequest(request))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo("search-error-id");
                    assertThat(response.getResult()).isNull();
                    assertThat(response.getError()).isNotNull();
                    assertThat(response.getError().getCode()).isEqualTo(-32602);
                    assertThat(response.getError().getMessage()).contains("Title parameter is required");
                })
                .verifyComplete();
    }

    @Test
    void handleGetMovieDetailsToolCall_WithValidData_ShouldReturnDetails() {
        // Given
        OmdbMovie movie = new OmdbMovie();
        movie.setTitle("Inception");
        movie.setYear("2010");
        movie.setDirector("Christopher Nolan");
        movie.setActors("Leonardo DiCaprio, Marion Cotillard");
        movie.setPlot("A thief who steals corporate secrets...");
        movie.setImdbRating("8.8");
        movie.setResponse("True");
        
        when(omdbService.getMovieByTitle("Inception", null, null))
                .thenReturn(Mono.just(movie));

        McpRequest request = new McpRequest();
        request.setId("details-id");
        request.setMethod("tools/call");
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", "get_movie_details");
        
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("title", "Inception");
        params.put("arguments", arguments);
        
        request.setParams(params);

        // When
        StepVerifier.create(mcpService.handleRequest(request))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo("details-id");
                    assertThat(response.getError()).isNull();
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) response.getResult();
                    
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
                    assertThat(content).hasSize(1);
                    
                    String text = (String) content.get(0).get("text");
                    assertThat(text).contains("🎬 Inception (2010)");
                    assertThat(text).contains("Christopher Nolan");
                    assertThat(text).contains("Leonardo DiCaprio");
                    assertThat(text).contains("8.8/10");
                })
                .verifyComplete();
    }

    @Test
    void handleGetMovieByImdbIdToolCall_WithValidId_ShouldReturnDetails() {
        // Given
        OmdbMovie movie = new OmdbMovie();
        movie.setTitle("The Dark Knight");
        movie.setYear("2008");
        movie.setDirector("Christopher Nolan");
        movie.setResponse("True");
        
        when(omdbService.getMovieByImdbId("tt0468569", null))
                .thenReturn(Mono.just(movie));

        McpRequest request = new McpRequest();
        request.setId("imdb-id");
        request.setMethod("tools/call");
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", "get_movie_by_imdb_id");
        
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("imdbId", "tt0468569");
        params.put("arguments", arguments);
        
        request.setParams(params);

        // When
        StepVerifier.create(mcpService.handleRequest(request))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo("imdb-id");
                    assertThat(response.getError()).isNull();
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) response.getResult();
                    
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
                    String text = (String) content.get(0).get("text");
                    assertThat(text).contains("The Dark Knight");
                    assertThat(text).contains("2008");
                })
                .verifyComplete();
    }

    @Test
    void handleUnknownMethod_ShouldReturnMethodNotFoundError() {
        // Given
        McpRequest request = new McpRequest();
        request.setId("unknown-id");
        request.setMethod("unknown/method");

        // When
        StepVerifier.create(mcpService.handleRequest(request))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo("unknown-id");
                    assertThat(response.getResult()).isNull();
                    assertThat(response.getError()).isNotNull();
                    assertThat(response.getError().getCode()).isEqualTo(-32601);
                    assertThat(response.getError().getMessage()).isEqualTo("Method not found");
                })
                .verifyComplete();
    }

    @Test
    void handleInvalidToolName_ShouldReturnInvalidToolError() {
        // Given
        McpRequest request = new McpRequest();
        request.setId("invalid-tool-id");
        request.setMethod("tools/call");
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", "invalid_tool");
        params.put("arguments", new HashMap<>());
        request.setParams(params);

        // When
        StepVerifier.create(mcpService.handleRequest(request))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo("invalid-tool-id");
                    assertThat(response.getResult()).isNull();
                    assertThat(response.getError()).isNotNull();
                    assertThat(response.getError().getCode()).isEqualTo(-32602);
                    assertThat(response.getError().getMessage()).isEqualTo("Invalid tool name");
                })
                .verifyComplete();
    }

    @Test
    void handleOmdbServiceError_ShouldReturnInternalError() {
        // Given
        when(omdbService.searchMovies(anyString(), eq(null), eq(null)))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        McpRequest request = new McpRequest();
        request.setId("error-id");
        request.setMethod("tools/call");
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", "search_movies");
        
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("title", "Test Movie");
        params.put("arguments", arguments);
        
        request.setParams(params);

        // When
        StepVerifier.create(mcpService.handleRequest(request))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo("error-id");
                    assertThat(response.getResult()).isNull();
                    assertThat(response.getError()).isNotNull();
                    assertThat(response.getError().getCode()).isEqualTo(-32603);
                    assertThat(response.getError().getMessage()).contains("Internal error");
                    assertThat(response.getError().getMessage()).contains("API Error");
                })
                .verifyComplete();
    }
}
