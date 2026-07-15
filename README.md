# ⚡ Tic-Tac-Toe Game Engine Service

Welcome to the **Tic-Tac-Toe Game Engine Service**. This reactive microservice manages core Tic-Tac-Toe game mechanics, applies turn validation and board calculations, tracks state using an embedded/persistent database, and asynchronously dispatches event payloads when games are completed.

---

## 🛠️ Tech Stack & Architecture

* **Backend Framework:** Spring Boot `4.0.x-SNAPSHOT` (Java `21`)
* **ORM & Database:** Spring Data JPA with an H2 Database (File-based Persistence support)
* **APIs:** REST (Web MVC Controller)
* **API Documentation:** Springdoc OpenAPI (Swagger UI)
* **Testing Engine:** JUnit 5, Mockito, MockMvc, and integration test suites

---

## 🚀 Quick Start Instructions

### 1. Prerequisites
Before running or building, ensure you have the following tools installed locally:
* **Java Development Kit (JDK):** Version `21` (required by configuration)
* **Apache Maven:** Version `3.9.x` or higher

### 2. Building the Project
Clean old build files, download fresh dependencies from the Spring repository, and package the microservice into a runnable JAR:

```bash
mvn clean package
```

To build the project without running the test suite, use:

```bash
mvn clean package -DskipTests
```
3. Running the Microservice
Start the application locally using the Spring Boot Maven plugin:

```bash
mvn spring-boot:run
```
Once running, the microservice spins up on Port 8080 (default) or the port configured in your properties.

Base URL: http://localhost:8080

Swagger UI Documentation: http://localhost:8080/swagger-ui/index.html

### 3. 💾 Database Persistence & Inspection
The service runs an H2 Database configured to run in File-Persistent Mode to secure data recovery across system restarts.

Configuration (src/main/resources/application.yml)
Ensure your database properties are set up to write to your local disk instead of wiping state:

```bash
YAML
spring:
  datasource:
    url: jdbc:h2:file:./data/gamedb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: password
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: update
  h2:
    console:
      enabled: true
      path: /h2-console
```

Accessing the DB Console
Open your browser and go to http://localhost:8080/h2-console. Fill out the connection prompt with:

JDBC URL: jdbc:h2:file:./data/gamedb

Username: sa
Password:

Click Connect to query, inspect, or manage historical game sessions.

### 4.🧪 Testing the Application
The codebase includes an enterprise-grade integration suite (GamePlayIntegrationTest) simulating a full multiplayer transaction where Player X lands a winning combination.

Run All Tests
Execute the entire test phase locally:

```bash
mvn test
```

Target Run Details
The integration test simulates the following sequence on a clean board:

* Initialize Session (POST /games) -> Verifies 201 Created status and fetches a fresh game UUID.

* Step-by-Step Moves (POST /games/{id}/move) -> Alternates turns between Alice (X) and Bob (O).

* Validates board win condition -> Confirms the state transitions to X_WON once 3 symbols line up.

* Validates Async Publish -> Asserts that GameEventPublisher is dispatched asynchronously exactly once.

### 5.🔌 API Reference & Playbook
#### 5.1. Start a New Game
Creates an empty board for two players.

Endpoint: POST /games

Request Payload (application/json):

```bash
JSON
{
  "playerXId": "player-alice",
  "playerOId": "player-bob"
}
Response (201 Created - Plain Text):

Plaintext
8c7e997f-94ab-42d4-a6fc-6e545465c1cc
```
#### 5.2. Fetch Current Game State
Retrieves the 3x3 board matrices, current active turn, and outcome status.

Endpoint: GET /games/{gameId}

Response Payload (200 OK):

```bash
JSON
{
  "gameId": "8c7e997f-94ab-42d4-a6fc-6e545465c1cc",
  "playerXId": "player-alice",
  "playerOId": "player-bob",
  "board": [
    ["", "", ""],
    ["", "", ""],
    ["", "", ""]
  ],
  "currentTurn": "X",
  "status": "IN_PROGRESS"
}
```
#### 5.3. Make a Move
Applies a marker to a target position. This action is validated strictly according to game logic.

Endpoint: POST /games/{gameId}/move

Request Payload (application/json):

```bash
JSON
{
  "playerId": "player-alice",
  "row": 0,
  "col": 0
}
```
Response Payload (200 OK):

```bash
JSON
{
  "gameId": "8c7e997f-94ab-42d4-a6fc-6e545465c1cc",
  "playerXId": "player-alice",
  "playerOId": "player-bob",
  "board": [
    ["X", "", ""],
    ["", "", ""],
    ["", "", ""]
  ],
  "currentTurn": "O",
  "status": "IN_PROGRESS"
}
```