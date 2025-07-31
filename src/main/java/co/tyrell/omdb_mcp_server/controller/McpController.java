package co.tyrell.omdb_mcp_server.controller;

import co.tyrell.omdb_mcp_server.model.mcp.McpRequest;
import co.tyrell.omdb_mcp_server.model.mcp.McpResponse;
import co.tyrell.omdb_mcp_server.service.McpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller for handling MCP requests
 */
@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class McpController {
    
    private final McpService mcpService;
    
    @PostMapping("")
    public Mono<ResponseEntity<McpResponse>> handleMcpRequest(@RequestBody McpRequest request) {
        log.info("Received MCP request: {} with ID: {}", request.getMethod(), request.getId());
        
        return mcpService.handleRequest(request)
                .map(response -> {
                    log.info("Sending MCP response for ID: {}", response.getId());
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(error -> {
                    log.error("Error processing MCP request", error);
                    McpResponse errorResponse = new McpResponse();
                    errorResponse.setId(request.getId());
                    
                    McpResponse.McpError mcpError = new McpResponse.McpError();
                    mcpError.setCode(-32603);
                    mcpError.setMessage("Internal error: " + error.getMessage());
                    errorResponse.setError(mcpError);
                    
                    return Mono.just(ResponseEntity.ok(errorResponse));
                });
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("MCP Server is running");
    }
}
