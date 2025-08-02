# OMDB MCP Server

[![CI](https://github.com/tyrell/omdb-mcp-server/actions/workflows/ci.yml/badge.svg)](https://github.com/tyrell/omdb-mcp-server/actions/workflows/ci.yml)
[![Build](https://github.com/tyrell/omdb-mcp-server/actions/workflows/build.yml/badge.svg)](https://github.com/tyrell/omdb-mcp-server/actions/workflows/build.yml)
[![Docker](https://github.com/tyrell/omdb-mcp-server/actions/workflows/docker.yml/## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

- 🐛 **Bug Reports**: [GitHub Issues](https://github.com/tyrell/omdb-mcp-server/issues)
- 💡 **Feature Requests**: [GitHub Discussions](https://github.com/tyrell/omdb-mcp-server/discussions)
- 📖 **Documentation**: [Project Wiki](https://github.com/tyrell/omdb-mcp-server/wiki)
- 🔒 **Security Issues**: Report security issues privately via GitHub Securitys://github.com/tyrell/omdb-mcp-server/actions/workflows/docker.yml)
[![Release](https://github.com/tyrell/omdb-mcp-server/actions/workflows/release.yml/badge.svg)](https://github.com/tyrell/omdb-mcp-server/actions/workflows/release.yml)
[![codecov](https://codecov.io/gh/tyrell/omdb-mcp-server/branch/main/graph/badge.svg)](https://codecov.io/gh/tyrell/omdb-mcp-server)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Available-blue.svg)](https://github.com/tyrell/omdb-mcp-server/pkgs/container/omdb-mcp-server)
[![MCP](https://img.shields.io/badge/MCP-2024--11--05-purple.svg)](https://spec.modelcontextprotocol.io/specification/)

A Model Context Protocol (MCP) Server that provides access to the Open Movie Database (OMDB) API. This server allows AI assistants and other MCP clients to search for movies and retrieve detailed movie information.

## Features

### 🎬 Movie Database Access
- **Search Movies**: Search for movies by title, year, and type
- **Get Movie Details**: Retrieve detailed information about a specific movie by title
- **Get Movie by IMDB ID**: Get detailed information using IMDB ID
- **Rich Metadata**: Access to ratings, cast, plot, awards, and more

### 🔌 MCP Protocol Compliance
- **MCP 2024-11-05**: Fully implements the latest MCP specification
- **JSON-RPC 2.0**: Standard protocol for communication
- **Tool Discovery**: Dynamic tool listing and schema validation
- **Error Handling**: Comprehensive error responses and validation

### 🚀 Production Ready
- **Docker Support**: Multi-platform container images (AMD64/ARM64)
- **Health Checks**: Built-in monitoring and health endpoints
- **Configuration**: Environment-based configuration
- **Logging**: Structured logging with configurable levels
- **Testing**: Comprehensive test suite with coverage reporting

### 🔧 Developer Experience
- **GitHub Actions**: Complete CI/CD pipeline
- **Automated Releases**: Tagged releases with artifacts
- **Security Scanning**: Vulnerability detection and reporting
- **Documentation**: Auto-generated API documentation
- **Code Quality**: Automated dependency updates and code analysis

## Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- OMDB API Key (free registration at http://www.omdbapi.com/apikey.aspx)

## Quick Start

### 🐳 Using Docker (Recommended)

```bash
# Pull and run the latest Docker image
docker run -p 8080:8080 -e OMDB_API_KEY=your-api-key ghcr.io/tyrell/omdb-mcp-server:latest
```

### 📦 Using Pre-built JAR

1. Download the latest JAR from [Releases](https://github.com/tyrell/omdb-mcp-server/releases)
2. Run with your OMDB API key:
   ```bash
   java -jar omdb-mcp-server-*.jar --omdb.api.key=your-api-key
   ```

### 🔨 Building from Source

1. **Clone and build the project**:
   ```bash
   git clone https://github.com/tyrell/omdb-mcp-server
   cd omdb-mcp-server
   ./mvnw clean package
   ```

2. **Set your OMDB API key**:
   ```bash
   export OMDB_API_KEY=your-actual-api-key-here
   ```

3. **Run the server**:
   ```bash
   ./mvnw spring-boot:run
   ```

The server will start on `http://localhost:8080`

## 🔧 Configuration

### Environment Variables
- `OMDB_API_KEY`: Your OMDB API key (required)
- `SERVER_PORT`: Server port (default: 8080)
- `MCP_SERVER_NAME`: MCP server name (default: "OMDB Movie Database Server")

### Docker Compose
```yaml
version: '3.8'
services:
  omdb-mcp-server:
    image: ghcr.io/tyrell/omdb-mcp-server:latest
    ports:
      - "8080:8080"
    environment:
      - OMDB_API_KEY=your-api-key-here
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 3s
      retries: 3
```

### Detailed Configuration

The server can be configured using environment variables or `application.properties`:

#### Core Configuration
```properties
# Server Configuration
server.port=8080

# OMDB API Configuration
omdb.api.url=https://www.omdbapi.com/
omdb.api.key=${OMDB_API_KEY:your-api-key-here}

# MCP Server Configuration
mcp.server.name=OMDB Movie Database Server
mcp.server.version=1.0.0
mcp.server.description=MCP Server for searching and retrieving movie information from OMDB API

# Logging Configuration
logging.level.co.tyrell.omdb_mcp_server=INFO
logging.level.root=WARN

# Actuator (Health Checks)
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized
```

#### Advanced Configuration
```properties
# WebClient Configuration
spring.webflux.timeout=30s
spring.reactor.netty.pool.max-connections=100

# JVM Tuning (for production)
JAVA_OPTS=-Xmx512m -Xms256m -XX:+UseG1GC
```

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

## 🧪 Testing & Validation

### Using the Test Script

A test script is provided to validate the MCP server functionality:

```bash
# Run the included test script
./test-mcp-server.sh
```

This script will test all MCP endpoints including initialization, tool listing, and all three movie search tools.

### 🔍 Development & Debugging

**Run tests**:
```bash
./mvnw test
```

**Generate test coverage report**:
```bash
./mvnw jacoco:report
# View report at target/site/jacoco/index.html
```

**Build Docker image locally**:
```bash
docker build -t omdb-mcp-server:local .
```

## 🚀 CI/CD & Deployment

This project includes comprehensive GitHub Actions workflows:

- **🔄 Continuous Integration**: Automated testing, linting, and security scanning
- **📦 Build & Package**: JAR and Docker image creation
- **🐳 Docker**: Multi-platform container builds (AMD64/ARM64)
- **🏷️ Release**: Automated releases with GitHub Releases and container registry
- **🔒 Security**: Vulnerability scanning with Trivy
- **📈 Dependencies**: Automated dependency updates

### Deployment Options

1. **GitHub Container Registry**:
   ```bash
   docker pull ghcr.io/tyrell/omdb-mcp-server:latest
   ```

2. **Manual JAR Deployment**:
   - Download from [GitHub Releases](https://github.com/tyrell/omdb-mcp-server/releases)
   - Deploy on any Java 21+ environment

3. **Kubernetes**:
   ```yaml
   apiVersion: apps/v1
   kind: Deployment
   metadata:
     name: omdb-mcp-server
   spec:
     replicas: 2
     selector:
       matchLabels:
         app: omdb-mcp-server
     template:
       metadata:
         labels:
           app: omdb-mcp-server
       spec:
         containers:
         - name: omdb-mcp-server
           image: ghcr.io/tyrell/omdb-mcp-server:latest
           ports:
           - containerPort: 8080
           env:
           - name: OMDB_API_KEY
             valueFrom:
               secretKeyRef:
                 name: omdb-secret
                 key: api-key
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

## 🏗️ Architecture & Technology Stack

### Technology Stack
- **☕ Java 21**: Modern Java with latest features
- **🍃 Spring Boot 3.5.4**: Production-ready application framework
- **⚡ Spring WebFlux**: Reactive programming for better performance
- **🐳 Docker**: Containerized deployment with multi-stage builds
- **🧪 JUnit 5**: Comprehensive testing framework
- **📊 JaCoCo**: Code coverage analysis
- **🔒 Security Scanning**: Automated vulnerability detection

### Architecture Overview
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   MCP Client    │───▶│  MCP Controller  │───▶│   MCP Service   │
│  (AI Assistant) │    │  (REST Layer)    │    │ (Protocol Impl) │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                                        │
                                                        ▼
                                                ┌─────────────────┐
                                                │  OMDB Service   │
                                                │ (External API)  │
                                                └─────────────────┘
                                                        │
                                                        ▼
                                                ┌─────────────────┐
                                                │   OMDB API      │
                                                │ (omdbapi.com)   │
                                                └─────────────────┘
```

### Key Components
- **🎮 McpController**: HTTP endpoint handling and request routing
- **🧠 McpService**: MCP protocol implementation and business logic
- **🌐 OmdbService**: OMDB API integration with reactive WebClient
- **📋 Model Classes**: Data structures for MCP and OMDB responses
- **⚙️ Configuration**: Spring Boot auto-configuration and properties

### Security Features
- 🔐 Non-root Docker user
- 🛡️ Automated vulnerability scanning
- 🚫 No sensitive data in images
- ✅ Health checks and monitoring
- 🔒 HTTPS support for external APIs

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

### Development Setup
1. **Fork the repository**
2. **Clone your fork**:
   ```bash
   git clone https://github.com/your-username/omdb-mcp-server
   cd omdb-mcp-server
   ```
3. **Set up development environment**:
   ```bash
   # Install Java 21 and Maven
   ./mvnw clean compile
   export OMDB_API_KEY=your-test-api-key
   ```
4. **Run tests**:
   ```bash
   ./mvnw test
   ```
5. **Make your changes and submit a PR**

### Code Style
- Follow Java conventions
- Write tests for new features
- Update documentation
- Ensure CI passes

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## � Support

- **Issues**: [GitHub Issues](https://github.com/tyrell/omdb-mcp-server/issues)
- **Discussions**: [GitHub Discussions](https://github.com/tyrell/omdb-mcp-server/discussions)
- **Security**: Report security issues privately via GitHub Security

## 📞 Support

- 🐛 **Bug Reports**: [GitHub Issues](https://github.com/tyrell/omdb-mcp-server/issues)
- 💡 **Feature Requests**: [GitHub Discussions](https://github.com/tyrell/omdb-mcp-server/discussions)
- 📖 **Documentation**: [Project Wiki](https://github.com/tyrell/omdb-mcp-server/wiki)
- 🔒 **Security Issues**: See [SECURITY.md](SECURITY.md)
