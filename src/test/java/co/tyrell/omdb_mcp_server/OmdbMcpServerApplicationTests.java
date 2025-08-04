package co.tyrell.omdb_mcp_server;

import co.tyrell.omdb_mcp_server.controller.McpController;
import co.tyrell.omdb_mcp_server.service.McpService;
import co.tyrell.omdb_mcp_server.service.OmdbService;
import co.tyrell.omdb_mcp_server.transport.StdioTransport;
import co.tyrell.omdb_mcp_server.config.McpProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "omdb.api.key=test-key",
    "mcp.server.name=Test OMDB MCP Server",
    "mcp.server.version=0.2.0-TEST", 
    "mcp.server.description=Test MCP server for OMDB movie database",
    "mcp.transport.stdio.enabled=true"  // Enable for testing
})
class OmdbMcpServerApplicationTests {

	@Autowired
	private McpController mcpController;
	
	@Autowired
	private McpService mcpService;
	
	@Autowired
	private OmdbService omdbService;
	
	@Autowired(required = false)  // Make optional since it's conditionally created
	private StdioTransport stdioTransport;
	
	@Autowired
	private McpProperties mcpProperties;

	@Test
	void contextLoads() {
		// Verify that all main components are loaded
		assertThat(mcpController).isNotNull();
		assertThat(mcpService).isNotNull();
		assertThat(omdbService).isNotNull();
		assertThat(stdioTransport).isNotNull();
		assertThat(mcpProperties).isNotNull();
	}

	@Test
	void healthEndpointWorks() {
		var response = mcpController.health();
		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getBody()).isEqualTo("MCP Server is running");
	}
	
	@Test
	void mcpPropertiesAreConfigured() {
		// Verify MCP properties are correctly configured
		assertThat(mcpProperties.getName()).isEqualTo("Test OMDB MCP Server");
		assertThat(mcpProperties.getVersion()).isEqualTo("0.2.0-TEST");
		assertThat(mcpProperties.getDescription()).isEqualTo("Test MCP server for OMDB movie database");
	}
	
	@Test
	void allRequiredBeansArePresent() {
		// Verify that Spring context contains all required beans for MCP functionality
		assertThat(mcpController).as("McpController should be present").isNotNull();
		assertThat(mcpService).as("McpService should be present").isNotNull();
		assertThat(omdbService).as("OmdbService should be present").isNotNull();
		assertThat(stdioTransport).as("StdioTransport should be present").isNotNull();
		assertThat(mcpProperties).as("McpProperties should be present").isNotNull();
	}
}
