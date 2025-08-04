package co.tyrell.omdb_mcp_server.transport;

import co.tyrell.omdb_mcp_server.model.mcp.McpRequest;
import co.tyrell.omdb_mcp_server.model.mcp.McpResponse;
import co.tyrell.omdb_mcp_server.service.McpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Stdio transport implementation for MCP protocol
 * This is the primary transport method used by most MCP clients
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "mcp.transport.stdio.enabled", havingValue = "true")
public class StdioTransport implements CommandLineRunner {

    private final McpService mcpService;
    
    // Create ObjectMapper directly since we may not have it as a bean
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(String... args) throws Exception {
        // Don't start stdio transport during tests
        if (isTestEnvironment()) {
            log.debug("Skipping stdio transport in test environment");
            return;
        }
        
        // Only start stdio transport if specifically requested
        if (isStdioMode(args)) {
            log.info("Starting MCP Server in stdio mode");
            handleStdioTransport();
        } else {
            log.debug("Stdio transport not enabled. Use --stdio flag or set MCP_STDIO_ENABLED=true");
        }
    }

    private boolean isTestEnvironment() {
        // Check if we're running in a test environment
        return System.getProperty("java.class.path", "").contains("test-classes") ||
               System.getProperty("surefire.test.class.path") != null ||
               "true".equals(System.getProperty("test.environment"));
    }

    private boolean isStdioMode(String[] args) {
        // Check if --stdio flag is present
        for (String arg : args) {
            if ("--stdio".equals(arg)) {
                return true;
            }
        }
        // Only enable stdio if explicitly requested via flag or environment variable
        return "true".equalsIgnoreCase(System.getenv("MCP_STDIO_ENABLED")) ||
               "true".equalsIgnoreCase(System.getProperty("mcp.stdio.enabled")) ||
               "true".equalsIgnoreCase(System.getProperty("mcp.transport.stdio.enabled"));
    }

    private void handleStdioTransport() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter writer = new PrintWriter(System.out, true)) {

            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    // Parse the JSON-RPC request
                    McpRequest request = objectMapper.readValue(line, McpRequest.class);
                    log.debug("Received stdio request: {}", request.getMethod());

                    // Process the request
                    mcpService.handleRequest(request)
                        .subscribe(
                            response -> {
                                try {
                                    String responseJson = objectMapper.writeValueAsString(response);
                                    writer.println(responseJson);
                                    writer.flush();
                                    log.debug("Sent stdio response for ID: {}", response.getId());
                                } catch (Exception e) {
                                    log.error("Error serializing response", e);
                                    sendErrorResponse(writer, request.getId(), -32603, "Internal error", null);
                                }
                            },
                            error -> {
                                log.error("Error processing request", error);
                                sendErrorResponse(writer, request.getId(), -32603, "Internal error: " + error.getMessage(), null);
                            }
                        );

                } catch (Exception e) {
                    log.error("Error parsing request: {}", line, e);
                    sendErrorResponse(writer, null, -32700, "Parse error", null);
                }
            }
        } catch (Exception e) {
            log.error("Error in stdio transport", e);
        }
    }

    private void sendErrorResponse(PrintWriter writer, String requestId, int code, String message, Object data) {
        try {
            McpResponse errorResponse = new McpResponse();
            errorResponse.setId(requestId);
            
            McpResponse.McpError error = new McpResponse.McpError();
            error.setCode(code);
            error.setMessage(message);
            error.setData(data);
            errorResponse.setError(error);

            String responseJson = objectMapper.writeValueAsString(errorResponse);
            writer.println(responseJson);
            writer.flush();
        } catch (Exception e) {
            log.error("Failed to send error response", e);
        }
    }
}
