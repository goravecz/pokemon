# Pokemon Battle API

A RESTful API for simulating Pokemon battles, built with Spring Boot 3 and Java 21.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Architecture](#architecture)
- [Testing](#testing)
- [Configuration](#configuration)
- [Production Considerations](#production-considerations)

## Features

- ✅ **Random Pokemon Generation**: Get two random Pokemon from the PokeAPI
- ✅ **Pokemon Battles**: Battle two Pokemon by name with random strength mechanics
- ✅ **Battle History**: Persistent storage of all battles with detailed participant information
- ✅ **Resilient External API Calls**: Configurable retry logic with exponential backoff
- ✅ **Input Validation**: Comprehensive validation including duplicate Pokemon detection
- ✅ **Error Handling**: RFC 7807 Problem Details for consistent error responses
- ✅ **OpenAPI Documentation**: Interactive Swagger UI for API exploration

## Tech Stack

- **Java 21**
- **Spring Boot 3.5.10**
- **PostgreSQL 15**
- **Spring Data JPA**
- **Spring Retry**
- **TestContainers**
- **JUnit 5**
- **Mockito**
- **JaCoCo**
- **Docker**
- **Lombok**
- **SpringDoc OpenAPI**

## Getting Started

### Prerequisites

- Java 21 (JDK 21+)
- Maven 3.9+
- Docker & Docker Compose (for containerized deployment)

### Running Locally with Docker

1**Start the application**
   ```bash
   docker-compose up --build
   ```

2**Access the API**
   - Base URL: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - API Docs: `http://localhost:8080/v3/api-docs`

### Running Tests

```bash
# Unit tests only
mvn test

# Integration tests only  
mvn verify -DskipUnitTests

# All tests with coverage
mvn clean verify

# View coverage report
open target/site/jacoco/index.html
```

## API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Endpoints

#### 1. Get Random Pokemon Pair

```http
GET /api/v1/pokemons
```

Returns two randomly selected Pokemon with generated strength values.

**Response:**
```json
{
  "pokemons": [
    {
      "name": "pikachu",
      "types": ["electric"],
      "imageUrl": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png",
      "strength": 15
    },
    {
      "name": "charizard",
      "types": ["fire", "flying"],
      "imageUrl": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/6.png",
      "strength": 12
    }
  ]
}
```

**Status Codes:**
- `200 OK` - Successfully returned Pokemon
- `500 Internal Server Error` - Failed to generate random Pokemon
- `502 Bad Gateway` - Network error reaching PokeAPI
- `503 Service Unavailable` - PokeAPI temporarily unavailable

---

#### 2. Battle Pokemon

```http
POST /api/v1/battles
Content-Type: application/json

{
  "pokemons": ["pikachu", "charizard"]
}
```

Simulates a battle between two Pokemon. Strength is randomly assigned, and the Pokemon with higher strength wins (first wins on ties).

**Request Validation:**
- Exactly 2 Pokemon required
- Pokemon names cannot be blank
- Pokemon names must be distinct (cannot battle same Pokemon)
- Pokemon names max length: 100 characters
- Only alphanumeric characters and hyphens allowed

**Response:**
```json
{
  "winner": {
    "name": "pikachu",
    "types": ["electric"],
    "imageUrl": "https://...",
    "strength": 15
  }
}
```

**Status Codes:**
- `200 OK` - Battle completed successfully
- `400 Bad Request` - Invalid request (validation errors)
- `404 Not Found` - One or both Pokemon not found in PokeAPI
- `502 Bad Gateway` - Network error reaching PokeAPI
- `503 Service Unavailable` - PokeAPI temporarily unavailable

---

#### 3. Get Battle History

```http
GET /api/v1/battles/history
```

Returns all past battles in descending order (most recent first).

**Response:**
```json
{
  "battles": [
    {
      "pokemons": [
        {
          "name": "pikachu",
          "types": ["electric"],
          "strength": 15
        },
        {
          "name": "charizard",
          "types": ["fire", "flying"],
          "strength": 12
        }
      ],
      "winner": "pikachu"
    }
  ]
}
```

**Status Codes:**
- `200 OK` - Successfully returned battle history
- `500 Internal Server Error` - Database error

## Configuration

**Development Profile** (`application-dev.yml`):
```yaml
pokeapi:
  base-url: https://pokeapi.co/api/v2
  pokemon-path: /pokemon/
  timeout:
    connect-ms: 5000
    read-ms: 10000
  retry:
    max-attempts: 3
    initial-backoff-ms: 100
    multiplier: 2.0
    max-interval: 10000
  generation:
    max-pokemon-id: 1024        # Pokemon ID range (Gen 1-8)
    max-strength: 20            # Maximum random strength
    max404-retries: 3           # Retries for missing Pokemon IDs
```

## Production Considerations

⚠️ **This is a development assignment, not production-ready.** Here's what would be needed for production:

### 1. Configuration Management
- **Base `application.yml`** with production defaults
- **Secrets Management**: Use Vault, AWS Secrets Manager, or Kubernetes Secrets
- **Environment-specific profiles**: dev, staging, prod
- **Externalized configuration**: ConfigMaps, environment variables

### 2. Security
- **Rate Limiting**: Prevent API abuse (e.g. Spring Cloud Gateway, Bucket4j)
- **Authentication & Authorization**: Spring Security with OAuth2/JWT
- **SQL Injection Protection**: Already handled by JPA, but validate all inputs
- **HTTPS Only**: TLS termination at load balancer or ingress

### 3. Observability
- **Structured Logging**: JSON logs with correlation IDs (MDC)
- **Distributed Tracing**: Spring Cloud Sleuth + Zipkin/Jaeger
- **Metrics**: Micrometer with Prometheus/Grafana
- **Health Checks**: Kubernetes liveness/readiness probes
- **Alerting**: On error rates, latency, external API failures

### 4. Performance
- **Caching**: Redis for Pokemon data (TTL: 1 hour)
  ```java
  @Cacheable(value = "pokemon", key = "#name")
  public Pokemon getPokemonByName(String name)
  ```
- **Pagination**: Battle history should use `Pageable` (Spring Data)
  ```java
  Page<BattleEntity> findAllByOrderByCreatedAtDesc(Pageable pageable)
  ```
- **Database Connection Pooling**: HikariCP (already default in Spring Boot)
- **Query Optimization**: Already addressed N+1 with `@EntityGraph`

### 5. Database
- **Schema Migrations**: Flyway or Liquibase (not `ddl-auto: update`)
- **Backup Strategy**: Automated daily backups, point-in-time recovery
- **Connection Pooling**: Tune HikariCP settings for load
- **Indexes**: Already covered by JPA, but monitor query performance
- **Read Replicas**: For battle history queries at scale

### 6. Resilience
- **Circuit Breaker**: Resilience4j for PokeAPI calls
- **Fallback Strategy**: Cached data or graceful degradation
- **Bulkhead Pattern**: Isolate thread pools for external calls
- **Timeout Configuration**: Already implemented, tune based on SLAs

### 7. API Versioning
- Current `/api/v1` is URI-based (good start)
- Consider content negotiation for breaking changes
- Deprecation strategy for old versions

### 8. Testing
- **Contract Testing**: Pact for API consumer contracts
- **Performance Testing**: JMeter or Gatling
- **Chaos Engineering**: Test failure scenarios
- **Security Testing**: OWASP ZAP, dependency scanning

### 9. Deployment
- **Container Registry**: ECR, GCR, or Docker Hub
- **Orchestration**: Kubernetes with Helm charts
- **CI/CD**: GitHub Actions, GitLab CI, or Jenkins
- **Blue-Green Deployments**: Zero-downtime releases
- **Infrastructure as Code**: Terraform or CloudFormation

### 10. Documentation
- **Architecture Decision Records (ADRs)**: Document key decisions
- **Runbooks**: Operational procedures for incidents
- **API Changelog**: Track breaking changes
- **SLA/SLO Documentation**: Performance targets
