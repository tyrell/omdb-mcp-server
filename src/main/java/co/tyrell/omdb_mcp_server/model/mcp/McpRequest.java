package co.tyrell.omdb_mcp_server.model.mcp;

import lombok.Data;

import java.util.Map;

/**
 * Base MCP request structure
 */
@Data
public class McpRequest {
    private String jsonrpc = "2.0";
    private String id;
    private String method;
    private Map<String, Object> params;
}
