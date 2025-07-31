package co.tyrell.omdb_mcp_server.model.mcp;

import lombok.Data;

import java.util.List;

/**
 * MCP Tool definition
 */
@Data
public class McpTool {
    private String name;
    private String description;
    private InputSchema inputSchema;
    
    @Data
    public static class InputSchema {
        private String type = "object";
        private Properties properties;
        private List<String> required;
        
        @Data
        public static class Properties {
            private Property title;
            private Property year;
            private Property type;
            private Property plot;
            private Property imdbId;
            
            @Data
            public static class Property {
                private String type;
                private String description;
            }
        }
    }
}
