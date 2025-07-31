package co.tyrell.omdb_mcp_server.model.mcp;

import lombok.Data;

/**
 * Base MCP response structure
 */
@Data
public class McpResponse {
    private String jsonrpc = "2.0";
    private String id;
    private Object result;
    private McpError error;
    
    @Data
    public static class McpError {
        private int code;
        private String message;
        private Object data;
    }
}
