package co.tyrell.omdb_mcp_server.transport;

import co.tyrell.omdb_mcp_server.service.McpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class StdioTransportTest {

    @Mock
    private McpService mcpService;

    private StdioTransport stdioTransport;

    @BeforeEach
    void setUp() {
        stdioTransport = new StdioTransport(mcpService);
    }

    @Test
    void isStdioMode_WithStdioFlag_ShouldReturnTrue() {
        // Given
        String[] args = {"--stdio"};

        // When
        Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(stdioTransport, "isStdioMode", (Object) args);

        // Then
        assertThat(result).isNotNull().isTrue();
    }

    @Test
    void isStdioMode_WithoutStdioFlag_ShouldReturnFalseByDefault() {
        // Given
        String[] args = {};
        
        // Clear any existing properties that might enable stdio
        System.clearProperty("server.port");
        System.clearProperty("mcp.transport.stdio.enabled");

        // When
        Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(stdioTransport, "isStdioMode", (Object) args);

        // Then
        assertThat(result).isNotNull().isFalse();
    }

    @Test
    void isStdioMode_WithEnvironmentVariable_ShouldReturnTrue() {
        // Given
        String[] args = {};
        // Note: We can't easily set environment variables in tests, 
        // but we can test system properties
        System.setProperty("mcp.transport.stdio.enabled", "true");

        try {
            // When
            Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(stdioTransport, "isStdioMode", (Object) args);

            // Then
            assertThat(result).isNotNull().isTrue();
        } finally {
            System.clearProperty("mcp.transport.stdio.enabled");
        }
    }

    @Test
    void isStdioMode_WithServerPortProperty_ShouldReturnFalse() {
        // Given
        String[] args = {};
        System.setProperty("server.port", "8080");

        try {
            // When
            Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(stdioTransport, "isStdioMode", (Object) args);

            // Then
            assertThat(result).isNotNull().isFalse();
        } finally {
            // Cleanup
            System.clearProperty("server.port");
        }
    }

    @Test
    void sendErrorResponse_ShouldCreateValidErrorResponse() {
        // Given
        String requestId = "test-123";
        int errorCode = -32601;
        String errorMessage = "Method not found";

        // Use a StringWriter to capture output instead of actual System.out
        java.io.StringWriter stringWriter = new java.io.StringWriter();
        java.io.PrintWriter writer = new java.io.PrintWriter(stringWriter);

        // When
        ReflectionTestUtils.invokeMethod(stdioTransport, "sendErrorResponse", writer, requestId, errorCode, errorMessage, null);

        // Then
        String output = stringWriter.toString();
        assertThat(output).isNotEmpty();
        
        // Verify it contains the expected error structure
        assertThat(output).contains("\"id\":\"test-123\"");
        assertThat(output).contains("\"code\":-32601");
        assertThat(output).contains("\"message\":\"Method not found\"");
    }

    @Test
    void sendErrorResponse_WithNullId_ShouldHandleCorrectly() {
        // Given
        String requestId = null;
        int errorCode = -32700;
        String errorMessage = "Parse error";

        java.io.StringWriter stringWriter = new java.io.StringWriter();
        java.io.PrintWriter writer = new java.io.PrintWriter(stringWriter);

        // When
        ReflectionTestUtils.invokeMethod(stdioTransport, "sendErrorResponse", writer, requestId, errorCode, errorMessage, null);

        // Then
        String output = stringWriter.toString();
        assertThat(output).isNotEmpty();
        assertThat(output).contains("\"id\":null");
        assertThat(output).contains("\"code\":-32700");
        assertThat(output).contains("\"message\":\"Parse error\"");
    }

    @Test
    void run_WithStdioArg_ShouldDetectStdioMode() throws Exception {
        // Given
        String[] args = {"--stdio"};

        // When - Create transport and verify it can handle stdio args
        StdioTransport transport = new StdioTransport(mcpService);
        
        // Then - Use reflection to verify the isStdioMode method works
        Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(transport, "isStdioMode", (Object) args);
        assertThat(result).isNotNull().isTrue();
    }

    @Test
    void run_WithoutStdioArg_ShouldNotStartStdioByDefault() throws Exception {
        // Given
        String[] args = {};
        // Ensure no stdio-enabling properties are set
        System.clearProperty("mcp.transport.stdio.enabled");

        try {
            // When
            StdioTransport transport = new StdioTransport(mcpService);
            Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(transport, "isStdioMode", (Object) args);
            
            // Then - should be false by default now
            assertThat(result).isNotNull().isFalse();
        } finally {
            // No cleanup needed since we're clearing
        }
    }
}
