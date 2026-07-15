# ⚡ Tic-Tac-Toe Game Session Service

Welcome to the **Game Session Service**. This microservice acts as the orchestrator/coordinator for Tic-Tac-Toe gameplay sessions. It provides endpoints to initialize session profiles in a local persistent database, communicate with a downstream **Game Engine Service** via modern HTTP client requests (`RestClient`), and automatically simulate complete end-to-end games utilizing random AI move selection.

---

## 🛠️ Tech Stack & Architecture

* **Backend Framework:** Spring Boot `4.0.8-SNAPSHOT` (Java `21`)
* **ORM & Database:** Spring Data JPA with an H2 Database (File-based Persistence support)
* **APIs:** RESTful endpoints (Spring Web MVC Controller with CORS enabled for frontend clients)
* **HTTP Client:** Spring's fluent `RestClient`
* **API Documentation:** Springdoc OpenAPI (Swagger UI)
* **Testing Engine:** JUnit 5, MockMvc, and integration tests utilizing **WireMock** for downstream REST stubbing

---

## 🚀 Quick Start Instructions

### 1. Prerequisites
Before running or building, ensure you have the following tools installed locally:
* **Java Development Kit (JDK):** Version `21`
* **Apache Maven:** Version `3.9.x` or higher

### 2. Building the Project
Clean old build files, download fresh dependencies from the repositories, and package the microservice into a runnable JAR:
```bash
mvn clean package
```
To build the project without running the test suite, use:

```bash
mvn clean package -DskipTests
```
### 3. Running the Microservice
Start the application locally using the Spring Boot Maven plugin:

```bash
mvn spring-boot:run
```
Once running, the microservice spins up on Port 8081 (default) to avoid port-binding conflicts with the downstream Game Engine (Port 8080):

* Base URL: http://localhost:8081

* Swagger UI Documentation: http://localhost:8081/swagger-ui.html

* OpenAPI Specs (JSON): http://localhost:8081/v3/api-docs

### 4. 💾 Database Persistence & Inspection
The service runs an embedded H2 Database configured in File-Persistent Mode to secure and recover your coordinator game sessions across application restarts.

#### Database Settings (application.yaml)
Path to Local Storage: ./data/sessions_db.mv.db

Hibernate Strategy: ddl-auto: update (preserves existing table schemas and records across restarts)

#### Accessing the Web Console
Open your browser and go to http://localhost:8081/h2-console.

Fill out the connection prompt with:

* Saved Settings: Generic H2 (Embedded)

* Driver Class: org.h2.Driver

* JDBC URL: jdbc:h2:file:./data/sessions_db

* User Name: sa

* Password: (leave blank)
 
* Click Connect to query, inspect, and manage active session tables.

### 5. 🧪 Testing the Application
The codebase includes an enterprise-grade integration suite (GameSessionIntegrationTest) utilizing WireMock to stub responses from the downstream Game Engine Service, simulating full end-to-end execution.

Run All Tests
Execute the entire test phase locally:

```bash
mvn test
```
#### Integration Test Logic
The test environment boots up on a random port using MockMvc and verifies the following workflow:

* Mock Setup (WireMock): Stubbing engine creation, current turn fetching, and move handling requests.

* Initialize Session (POST /sessions) -> Verifies 201 Created status, local persistence, and matches the session details.

* Trigger Simulation (POST /sessions/{sessionId}/simulate) -> Orchestrates mock engine calls, automates game mechanics until completion, and updates the local state to FINISHED.

* Fetch Session (GET /sessions/{sessionId}) -> Asserts database persistence integrity and structural verification.

### 6. 🔌 API Reference & Playbook
#### 6.1. Create a Game Session
Initializes a new game coordinator profile and provisions a corresponding state inside the downstream Game Engine.

Endpoint: POST /sessions

Request Payload (application/json):
```bash
JSON
{
  "playerXId": "player-alice",
  "playerOId": "player-bob"
}
```
Response Payload (201 Created):
```bash
JSON
{
  "sessionId": "4b6c86a9-063f-4e09-8b27-56e297800c8a",
  "playerXId": "player-alice",
  "playerOId": "player-bob",
  "status": "CREATED",
  "gameResult": null,
  "moveHistory": []
}
```
#### 6.2. Fetch Session Profile
Retrieves active status, player mappings, and logged move-by-move histories.

Endpoint: GET /sessions/{sessionId}

Response Payload (200 OK):
```bash
JSON
{
  "sessionId": "4b6c86a9-063f-4e09-8b27-56e297800c8a",
  "playerXId": "player-alice",
  "playerOId": "player-bob",
  "status": "CREATED",
  "gameResult": null,
  "moveHistory": []
}
```
#### 6.3. Simulate Complete Gameplay
Triggers an automated game loop making random moves for both players until a draw or win is achieved on the board.

Endpoint: POST /sessions/{sessionId}/simulate

Response Payload (200 OK):
```bash
JSON
{
  "sessionId": "4b6c86a9-063f-4e09-8b27-56e297800c8a",
  "playerXId": "player-alice",
  "playerOId": "player-bob",
  "status": "FINISHED",
  "gameResult": "X_WON",
  "moveHistory": [
    {
      "playerId": "player-alice",
      "row": 0,
      "col": 0,
      "symbol": "X"
    },
    {
      "playerId": "player-bob",
      "row": 1,
      "col": 1,
      "symbol": "O"
    },
    {
      "playerId": "player-alice",
      "row": 0,
      "col": 1,
      "symbol": "X"
    },
    {
      "playerId": "player-bob",
      "row": 2,
      "col": 2,
      "symbol": "O"
    },
    {
      "playerId": "player-alice",
      "row": 0,
      "col": 2,
      "symbol": "X"
    }
  ]
}
```
