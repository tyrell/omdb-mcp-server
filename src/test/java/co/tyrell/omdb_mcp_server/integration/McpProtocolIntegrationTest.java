package co.tyrell.omdb_mcp_server.integration;

import co.tyrell.omdb_mcp_server.model.mcp.McpRequest;
import co.tyrell.omdb_mcp_server.model.mcp.McpResponse;
import co.tyrell.omdb_mcp_server.model.mcp.McpTool;
import co.tyrell.omdb_mcp_server.service.McpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for MCP protocol compliance
 */
@SpringBootTest
@TestPropertySource(properties = {
    "omdb.api.key=test-key",
    "mcp.name=Integration Test OMDB MCP Server",
    "mcp.version=0.2.0-INTEGRATION",
    "mcp.description=Integration test MCP server for OMDB movie database"
})
class McpProtocolIntegrationTest {

    @Autowired
    private McpService mcpService;

    @Test
    void fullMcpHandshake_ShouldCompleteSuccessfully() {
        // Test the complete MCP handshake sequence
        
        // 1. Initialize request
        McpRequest initRequest = new McpRequest();
        initRequest.setId("init-1");
        initRequest.setMethod("initialize");
        
        Map<String, Object> initParams = new HashMap<>();
        initParams.put("protocolVersion", "2024-11-05");
        initParams.put("clientInfo", Map.of("name", "TestClient", "version", "1.0.0"));
        initRequest.setParams(initParams);

        StepVerifier.create(mcpService.handleRequest(initRequest))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo("init-1");
                    assertThat(response.getError()).isNull();
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) response.getResult();
                    assertThat(result.get("protocolVersion")).isEqualTo("2024-11-05");
                    assertThat(result).containsKey("capabilities");
                    assertThat(result).containsKey("serverInfo");
                })
                .verifyComplete();

        // 2. Initialized notification (should not return response)
        McpRequest initNotification = new McpRequest();
        initNotification.setMethod("notifications/initialized");
        initNotification.setParams(Map.of());

        StepVerifier.create(mcpService.handleRequest(initNotification))
                .verifyComplete();

        // 3. Tools list request
        McpRequest toolsRequest = new McpRequest();
        toolsRequest.setId("tools-1");
        toolsRequest.setMethod("tools/list");

        StepVerifier.create(mcpService.handleRequest(toolsRequest))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo("tools-1");
                    assertThat(response.getError()).isNull();
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) response.getResult();
                    
                    @SuppressWarnings("unchecked")
                    List<?> tools = (List<?>) result.get("tools");
                    assertThat(tools).hasSize(3);
                })
                .verifyComplete();

        // 4. Ping request
        McpRequest pingRequest = new McpRequest();
        pingRequest.setId("ping-1");
        pingRequest.setMethod("ping");

        StepVerifier.create(mcpService.handleRequest(pingRequest))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo("ping-1");
                    assertThat(response.getError()).isNull();
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) response.getResult();
                    assertThat(result.get("pong")).isEqualTo(true);
                })
                .verifyComplete();
    }

    @Test
    void mcpErrorHandling_ShouldFollowJsonRpcSpec() {
        // Test error handling according to JSON-RPC spec
        
        // Invalid method
        McpRequest invalidMethod = new McpRequest();
        invalidMethod.setId("error-1");
        invalidMethod.setMethod("invalid/method");

        StepVerifier.create(mcpService.handleRequest(invalidMethod))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo("error-1");
                    assertThat(response.getResult()).isNull();
                    assertThat(response.getError()).isNotNull();
                    assertThat(response.getError().getCode()).isEqualTo(-32601); // Method not found
                })
                .verifyComplete();

        // Invalid parameters for tool call
        McpRequest invalidParams = new McpRequest();
        invalidParams.setId("error-2");
        invalidParams.setMethod("tools/call");
        invalidParams.setParams(Map.of("name", "search_movies", "arguments", Map.of())); // Missing required title

        StepVerifier.create(mcpService.handleRequest(invalidParams))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo("error-2");
                    assertThat(response.getResult()).isNull();
                    assertThat(response.getError()).isNotNull();
                    assertThat(response.getError().getCode()).isEqualTo(-32602); // Invalid params
                })
                .verifyComplete();
    }

    @Test
    void toolSchemaValidation_ShouldProvideComprehensiveInformation() {
        // Test that tool schemas provide all necessary information for AI clients
        
        McpRequest toolsRequest = new McpRequest();
        toolsRequest.setId("schema-test");
        toolsRequest.setMethod("tools/list");

        StepVerifier.create(mcpService.handleRequest(toolsRequest))
                .assertNext(response -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) response.getResult();
                    
                    @SuppressWarnings("unchecked")
                    List<McpTool> toolsList = (List<McpTool>) result.get("tools");
                    assertThat(toolsList).hasSize(3);
                    
                    // Verify each tool has proper schema structure
                    toolsList.forEach(tool -> {
                        // Every tool should have these fields
                        assertThat(tool.getName()).isNotNull();
                        assertThat(tool.getDescription()).isNotNull();
                        assertThat(tool.getInputSchema()).isNotNull();
                        
                        McpTool.InputSchema schema = tool.getInputSchema();
                        assertThat(schema.getProperties()).isNotNull();
                        assertThat(schema.getRequired()).isNotNull();
                        
                        // Properties should have detailed validation
                        assertThat(schema.getProperties()).isNotEmpty();
                    });
                })
                .verifyComplete();
    }
}
