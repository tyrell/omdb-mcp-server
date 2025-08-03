# OMDB MCP Server - LLM Integration Guide

## Overview

This guide explains how to integrate the OMDB MCP Server with Large Language Models (LLMs) to enable AI assistants to search for movies and retrieve detailed movie information from the Open Movie Database (OMDB).

The OMDB MCP Server implements the Model Context Protocol (MCP) 2024-11-05 specification, making it compatible with various AI assistants and LLM clients that support MCP.

## Table of Contents

1. [Understanding the Architecture](#understanding-the-architecture)
2. [Prerequisites](#prerequisites)
3. [Setting Up the MCP Server](#setting-up-the-mcp-server)
4. [MCP Client Integration](#mcp-client-integration)
5. [Tool Usage Patterns](#tool-usage-patterns)
6. [Example Conversations](#example-conversations)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)

## Understanding the Architecture

### Model Context Protocol (MCP)

The Model Context Protocol is a standard that allows AI assistants to access external tools and data sources. The OMDB MCP Server acts as a bridge between your LLM and the OMDB API, providing these capabilities:

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   LLM Client    │───▶│  OMDB MCP Server │───▶│   OMDB API      │
│  (AI Assistant) │    │  (This Project)  │    │ (omdbapi.com)   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
        │                       │                       │
        │                       ▼                       │
        │               ┌──────────────────┐            │
        └──────────────▶│  Cached Results  │◀───────────┘
                        │  (In Memory)     │
                        └──────────────────┘
```

### Available Tools

The server provides three main tools to LLMs:

1. **`search_movies`** - Search for movies by title, year, and type
2. **`get_movie_details`** - Get detailed information about a specific movie
3. **`get_movie_by_imdb_id`** - Retrieve movie details using IMDB ID

## Prerequisites

### For the MCP Server

- Java 23 or higher
- OMDB API Key (free from http://www.omdbapi.com/apikey.aspx)
- Docker (optional, for containerized deployment)

### For LLM Integration

- An LLM client that supports MCP (e.g., Claude Desktop, OpenAI ChatGPT with plugins, custom clients)
- Network access to the MCP server
- Basic understanding of JSON-RPC 2.0 protocol

## Setting Up the MCP Server

### Option 1: Docker (Recommended)

```bash
# Pull and run the latest Docker image
docker run -d \
  --name omdb-mcp-server \
  -p 8081:8081 \
  -e OMDB_API_KEY=your-actual-api-key-here \
  ghcr.io/tyrell/omdb-mcp-server:latest
```

### Option 2: Pre-built JAR

```bash
# Download the latest JAR from GitHub Releases
wget https://github.com/tyrell/omdb-mcp-server/releases/latest/download/omdb-mcp-server.jar

# Run with your API key
java -jar omdb-mcp-server.jar --omdb.api.key=your-actual-api-key-here
```

### Option 3: Build from Source

```bash
# Clone and build
git clone https://github.com/tyrell/omdb-mcp-server
cd omdb-mcp-server
./mvnw clean package

# Set API key and run
export OMDB_API_KEY=your-actual-api-key-here
./mvnw spring-boot:run
```

### Verify Installation

Once running, verify the server is working:

```bash
# Health check
curl http://localhost:8081/mcp/health

# View API documentation
open http://localhost:8081/swagger-ui/index.html
```

## MCP Client Integration

### Configuration for Claude Desktop

Add this configuration to your Claude Desktop MCP settings:

```json
{
  "mcpServers": {
    "omdb": {
      "command": "java",
      "args": [
        "-jar", 
        "/path/to/omdb-mcp-server.jar"
      ],
      "env": {
        "OMDB_API_KEY": "your-actual-api-key-here",
        "SERVER_PORT": "8081"
      }
    }
  }
}
```

### Configuration for Custom MCP Clients

For custom implementations, connect to the MCP server at `http://localhost:8081/mcp` using JSON-RPC 2.0 protocol.

## Tool Usage Patterns

### 1. Initialize Connection

Before using any tools, initialize the MCP connection:

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "initialize",
  "params": {
    "protocolVersion": "2024-11-05",
    "capabilities": {},
    "clientInfo": {
      "name": "my-llm-client",
      "version": "1.0.0"
    }
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "protocolVersion": "2024-11-05",
    "capabilities": {
      "tools": {}
    },
    "serverInfo": {
      "name": "OMDB Movie Database Server",
      "version": "1.0.0"
    }
  }
}
```

### 2. Discover Available Tools

```json
{
  "jsonrpc": "2.0",
  "id": "2",
  "method": "tools/list"
}
```

This returns a list of all available tools with their schemas and descriptions.

### 3. Search for Movies

```json
{
  "jsonrpc": "2.0",
  "id": "3",
  "method": "tools/call",
  "params": {
    "name": "search_movies",
    "arguments": {
      "title": "Matrix",
      "year": "1999",
      "type": "movie"
    }
  }
}
```

**Parameters:**
- `title` (required): Movie title to search for
- `year` (optional): Year of release
- `type` (optional): "movie", "series", or "episode"

### 4. Get Detailed Movie Information

```json
{
  "jsonrpc": "2.0",
  "id": "4",
  "method": "tools/call",
  "params": {
    "name": "get_movie_details",
    "arguments": {
      "title": "The Matrix",
      "year": "1999",
      "plot": "full"
    }
  }
}
```

**Parameters:**
- `title` (required): Movie title
- `year` (optional): Year of release
- `plot` (optional): "short" or "full" (default: "full")

### 5. Get Movie by IMDB ID

```json
{
  "jsonrpc": "2.0",
  "id": "5",
  "method": "tools/call",
  "params": {
    "name": "get_movie_by_imdb_id",
    "arguments": {
      "imdbId": "tt0133093",
      "plot": "full"
    }
  }
}
```

**Parameters:**
- `imdbId` (required): IMDB ID (e.g., "tt0133093")
- `plot` (optional): "short" or "full" (default: "full")

## Example Conversations

### Conversation 1: Movie Recommendation

**User:** "Can you help me find some good sci-fi movies from the 1990s?"

**LLM Process:**
1. Use `search_movies` with title "sci-fi" or search for specific known titles
2. Use `get_movie_details` for promising results
3. Present formatted recommendations

**Example LLM calls:**
```json
[
  {
    "method": "tools/call",
    "params": {
      "name": "search_movies",
      "arguments": {"title": "Matrix", "year": "1999"}
    }
  },
  {
    "method": "tools/call",
    "params": {
      "name": "get_movie_details",
      "arguments": {"title": "The Matrix", "year": "1999"}
    }
  }
]
```

### Conversation 2: Movie Information Lookup

**User:** "Tell me about the movie with IMDB ID tt0111161"

**LLM Process:**
1. Use `get_movie_by_imdb_id` with the provided IMDB ID
2. Format and present the detailed information

**LLM call:**
```json
{
  "method": "tools/call",
  "params": {
    "name": "get_movie_by_imdb_id",
    "arguments": {"imdbId": "tt0111161"}
  }
}
```

### Conversation 3: Comparative Analysis

**User:** "Compare the ratings and reviews of The Godfather and The Godfather Part II"

**LLM Process:**
1. Use `get_movie_details` for "The Godfather"
2. Use `get_movie_details` for "The Godfather Part II"
3. Compare and contrast the information

## Best Practices

### For LLM Developers

1. **Error Handling**: Always check for error responses and handle them gracefully
   ```json
   {
     "jsonrpc": "2.0",
     "id": "1",
     "error": {
       "code": -32602,
       "message": "Invalid params",
       "data": null
     }
   }
   ```

2. **Caching Awareness**: The server caches responses automatically, so repeated queries are fast and don't consume additional API credits

3. **Rate Limiting**: While the server handles OMDB API rate limits, be mindful of making excessive requests

4. **Search Strategy**: 
   - Start with broad searches using `search_movies`
   - Use specific details with `get_movie_details` for exact matches
   - Use `get_movie_by_imdb_id` when you have the exact IMDB ID

### For End Users

1. **Be Specific**: More specific movie titles and years yield better results
2. **Use Context**: Mention directors, actors, or plot details to help disambiguation
3. **Year Information**: Including the release year greatly improves accuracy

### Performance Optimization

1. **Leverage Caching**: Identical requests are served from cache instantly
2. **Monitor Cache Performance**: Use `/cache/stats` endpoint to check hit rates
3. **Clear Cache When Needed**: Use `/cache/clear` for testing or debugging

```bash
# Check cache performance
curl http://localhost:8081/cache/stats

# Clear all caches
curl -X DELETE http://localhost:8081/cache/clear
```

## Error Handling

### Common Error Codes

- **-32700**: Parse error (malformed JSON)
- **-32600**: Invalid request (missing required fields)
- **-32601**: Method not found (unsupported MCP method)
- **-32602**: Invalid params (missing or invalid parameters)
- **-32603**: Internal error (server-side error)

### Example Error Response

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "error": {
    "code": -32602,
    "message": "Title parameter is required",
    "data": null
  }
}
```

### Handling Movie Not Found

When a movie isn't found, the server returns a successful response with an error message in the content:

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "content": [
      {
        "type": "text",
        "text": "Movie not found: Movie not found!"
      }
    ]
  }
}
```

## Troubleshooting

### Server Issues

1. **Server Won't Start**
   - Check Java version (requires Java 23+)
   - Verify OMDB API key is set correctly
   - Check port 8081 isn't already in use

2. **API Key Issues**
   - Verify your OMDB API key at http://www.omdbapi.com/
   - Check environment variable is set: `echo $OMDB_API_KEY`
   - For free keys, check daily request limits

3. **Network Issues**
   - Verify server is running: `curl http://localhost:8081/mcp/health`
   - Check firewall settings
   - Verify Docker port mapping (if using Docker)

### LLM Integration Issues

1. **Connection Refused**
   - Check MCP server is running and accessible
   - Verify correct server URL and port in MCP configuration
   - Check network connectivity

2. **Tool Not Available**
   - Verify MCP initialization was successful
   - Check tool names match exactly: `search_movies`, `get_movie_details`, `get_movie_by_imdb_id`
   - Review tool list response for available tools

3. **Authentication Errors**
   - Check OMDB API key is valid and not expired
   - Verify API key has sufficient quota
   - Monitor server logs for OMDB API errors

### Debugging Tips

1. **Enable Debug Logging**
   ```bash
   # Add to application.properties or environment
   LOGGING_LEVEL_CO_TYRELL_OMDB_MCP_SERVER=DEBUG
   ```

2. **Monitor Cache Statistics**
   ```bash
   curl http://localhost:8081/cache/stats | jq
   ```

3. **Test MCP Protocol Directly**
   ```bash
   # Use the provided test script
   ./test-mcp-server.sh
   ```

4. **Check Server Health**
   ```bash
   curl http://localhost:8081/actuator/health
   ```

## Advanced Configuration

### Custom Port Configuration

```bash
# Change server port
java -jar omdb-mcp-server.jar --server.port=9090
```

### Cache Configuration

```properties
# Extend cache TTL to 2 hours
cache.expire-after-write=2h

# Increase cache size
cache.maximum-size=2000

# Disable cache statistics (for performance)
cache.record-stats=false
```

### Production Deployment

For production use, consider:

1. **Docker Compose Setup**
   ```yaml
   version: '3.8'
   services:
     omdb-mcp-server:
       image: ghcr.io/tyrell/omdb-mcp-server:latest
       ports:
         - "8081:8081"
       environment:
         - OMDB_API_KEY=${OMDB_API_KEY}
       restart: unless-stopped
       healthcheck:
         test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8081/actuator/health"]
         interval: 30s
         timeout: 3s
         retries: 3
   ```

2. **Load Balancing**: Deploy multiple instances behind a load balancer
3. **Monitoring**: Use the provided health checks and metrics endpoints
4. **Security**: Run behind a reverse proxy with proper security headers

## Integration Examples

### Python MCP Client

```python
import requests
import json

class OmdbMcpClient:
    def __init__(self, server_url="http://localhost:8081/mcp"):
        self.server_url = server_url
        self.request_id = 0
    
    def _make_request(self, method, params=None):
        self.request_id += 1
        payload = {
            "jsonrpc": "2.0",
            "id": str(self.request_id),
            "method": method,
            "params": params or {}
        }
        
        response = requests.post(
            self.server_url,
            headers={"Content-Type": "application/json"},
            data=json.dumps(payload)
        )
        return response.json()
    
    def initialize(self):
        return self._make_request("initialize", {
            "protocolVersion": "2024-11-05",
            "capabilities": {},
            "clientInfo": {
                "name": "python-client",
                "version": "1.0.0"
            }
        })
    
    def list_tools(self):
        return self._make_request("tools/list")
    
    def search_movies(self, title, year=None, type=None):
        arguments = {"title": title}
        if year:
            arguments["year"] = year
        if type:
            arguments["type"] = type
            
        return self._make_request("tools/call", {
            "name": "search_movies",
            "arguments": arguments
        })

# Usage example
client = OmdbMcpClient()
client.initialize()
result = client.search_movies("Matrix", "1999")
print(result)
```

### Node.js MCP Client

```javascript
const axios = require('axios');

class OmdbMcpClient {
    constructor(serverUrl = 'http://localhost:8081/mcp') {
        this.serverUrl = serverUrl;
        this.requestId = 0;
    }
    
    async makeRequest(method, params = {}) {
        this.requestId++;
        const payload = {
            jsonrpc: '2.0',
            id: this.requestId.toString(),
            method,
            params
        };
        
        const response = await axios.post(this.serverUrl, payload, {
            headers: { 'Content-Type': 'application/json' }
        });
        
        return response.data;
    }
    
    async initialize() {
        return this.makeRequest('initialize', {
            protocolVersion: '2024-11-05',
            capabilities: {},
            clientInfo: {
                name: 'nodejs-client',
                version: '1.0.0'
            }
        });
    }
    
    async searchMovies(title, year = null, type = null) {
        const arguments = { title };
        if (year) arguments.year = year;
        if (type) arguments.type = type;
        
        return this.makeRequest('tools/call', {
            name: 'search_movies',
            arguments
        });
    }
}

// Usage example
(async () => {
    const client = new OmdbMcpClient();
    await client.initialize();
    const result = await client.searchMovies('Matrix', '1999');
    console.log(result);
})();
```

## Conclusion

The OMDB MCP Server provides a robust, cacheable interface for LLMs to access movie information from the OMDB database. By following this guide, you can successfully integrate movie lookup capabilities into your AI assistant, enabling rich conversations about films, movie recommendations, and detailed movie information retrieval.

The server's intelligent caching system ensures optimal performance while respecting API rate limits, making it suitable for both development and production use cases.

For additional support, refer to the project's [GitHub repository](https://github.com/tyrell/omdb-mcp-server) or the comprehensive [API documentation](http://localhost:8081/swagger-ui/index.html) when the server is running.
