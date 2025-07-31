# OMDB MCP Server

A Model Context Protocol (MCP) Server that provides access to the Open Movie Database (OMDB) API. This server allows AI assistants and other MCP clients to search for movies and retrieve detailed movie information.

## Features

- **Search Movies**: Search for movies by title, year, and type
- **Get Movie Details**: Retrieve detailed information about a specific movie by title
- **Get Movie by IMDB ID**: Get detailed information using IMDB ID
- **MCP Protocol Compliance**: Fully implements the MCP 2024-11-05 specification

## Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- OMDB API Key (free registration at http://www.omdbapi.com/apikey.aspx)

## Setup

1. **Clone and build the project**:
   ```bash
   git clone <repository-url>
   cd omdb-mcp-server
   mvn clean compile
   ```

2. **Set your OMDB API key**:
   You can set it as an environment variable:
   ```bash
   export OMDB_API_KEY=your-actual-api-key-here
   ```

   Or modify `src/main/resources/application.properties`:
   ```properties
   omdb.api.key=your-actual-api-key-here
   ```

3. **Run the server**:
   ```bash
   mvn spring-boot:run
   ```

   The server will start on `http://localhost:8080`

## MCP Tools

### 1. search_movies
Search for movies by title in the OMDB database.

**Parameters**:
- `title` (required): Movie title to search for
- `year` (optional): Year of release
- `type` (optional): Type of result (movie, series, or episode)

**Example**:
```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "tools/call",
  "params": {
    "name": "search_movies",
    "arguments": {
      "title": "The Matrix",
      "year": "1999"
    }
  }
}
```

### 2. get_movie_details
Get detailed information about a specific movie by title.

**Parameters**:
- `title` (required): Movie title
- `year` (optional): Year of release
- `plot` (optional): Plot length ("short" or "full", default: "full")

**Example**:
```json
{
  "jsonrpc": "2.0",
  "id": "2",
  "method": "tools/call",
  "params": {
    "name": "get_movie_details",
    "arguments": {
      "title": "The Shawshank Redemption",
      "plot": "full"
    }
  }
}
```

### 3. get_movie_by_imdb_id
Get detailed information about a movie by IMDB ID.

**Parameters**:
- `imdbId` (required): IMDB ID (e.g., "tt0111161")
- `plot` (optional): Plot length ("short" or "full", default: "full")

**Example**:
```json
{
  "jsonrpc": "2.0",
  "id": "3",
  "method": "tools/call",
  "params": {
    "name": "get_movie_by_imdb_id",
    "arguments": {
      "imdbId": "tt0111161"
    }
  }
}
```

## MCP Protocol Implementation

### Initialize
```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "initialize",
  "params": {
    "protocolVersion": "2024-11-05",
    "capabilities": {},
    "clientInfo": {
      "name": "example-client",
      "version": "1.0.0"
    }
  }
}
```

### List Tools
```json
{
  "jsonrpc": "2.0",
  "id": "2",
  "method": "tools/list"
}
```

## Configuration

The server can be configured using the following properties in `application.properties`:

```properties
# Server Configuration
server.port=8080

# OMDB API Configuration
omdb.api.url=https://www.omdbapi.com/
omdb.api.key=${OMDB_API_KEY:your-api-key-here}}

# MCP Server Configuration
mcp.server.name=OMDB Movie Database Server
mcp.server.version=1.0.0
mcp.server.description=MCP Server for searching and retrieving movie information from OMDB API

# Logging
logging.level.co.tyrell.omdb_mcp_server=DEBUG
```

## Testing

Test the server health:
```bash
curl http://localhost:8080/mcp/health
```

Test MCP initialization:
```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "initialize",
    "params": {
      "protocolVersion": "2024-11-05",
      "capabilities": {},
      "clientInfo": {
        "name": "test-client",
        "version": "1.0.0"
      }
    }
  }'
```

Test tools listing:
```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "2",
    "method": "tools/list"
  }'
```

## API Response Format

All responses follow the MCP protocol format:

```json
{
  "jsonrpc": "2.0",
  "id": "request-id",
  "result": {
    "content": [
      {
        "type": "text",
        "text": "Formatted movie information..."
      }
    ]
  }
}
```

## Error Handling

The server returns standard JSON-RPC error responses:

```json
{
  "jsonrpc": "2.0",
  "id": "request-id",
  "error": {
    "code": -32602,
    "message": "Invalid params",
    "data": null
  }
}
```

Common error codes:
- `-32700`: Parse error
- `-32600`: Invalid request
- `-32601`: Method not found
- `-32602`: Invalid params
- `-32603`: Internal error

## Architecture

- **Controller Layer**: `McpController` handles HTTP requests
- **Service Layer**: `McpService` implements MCP protocol, `OmdbService` handles OMDB API calls
- **Model Layer**: MCP and OMDB data models
- **Configuration**: Spring Boot configuration for properties and web client

## License

This project is open source and available under the [MIT License](LICENSE).
