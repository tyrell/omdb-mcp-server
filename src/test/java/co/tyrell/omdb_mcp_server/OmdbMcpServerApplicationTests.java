package co.tyrell.omdb_mcp_server;

import co.tyrell.omdb_mcp_server.controller.McpController;
import co.tyrell.omdb_mcp_server.service.McpService;
import co.tyrell.omdb_mcp_server.service.OmdbService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OmdbMcpServerApplicationTests {

	@Autowired
	private McpController mcpController;
	
	@Autowired
	private McpService mcpService;
	
	@Autowired
	private OmdbService omdbService;

	@Test
	void contextLoads() {
		// Verify that all main components are loaded
		assertThat(mcpController).isNotNull();
		assertThat(mcpService).isNotNull();
		assertThat(omdbService).isNotNull();
	}

	@Test
	void healthEndpointWorks() {
		var response = mcpController.health();
		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getBody()).isEqualTo("MCP Server is running");
	}
}
