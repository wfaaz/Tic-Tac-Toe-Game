[README.md](https://github.com/user-attachments/files/30057844/README.md)
# ⚡ Tic-Tac-Toe Game Frontend Service

Welcome to the **Frontend Microservice for Tic-Tac-Toe Simulation**. This lightweight client interface serves as an interactive web-based dashboard (using Tailwind CSS) allowing developers to visually simulate end-to-end Tic-Tac-Toe game flows. It interacts with the backend **Game Session Service** to trigger a game loop, then dynamically animations-plays each move step-by-step with real-time logging.

---

## 🛠️ Tech Stack & Architecture

* **Runtime & Backend server:** Node.js with Express (`server.js`)
* **Styling Framework:** Tailwind CSS (via CDN)
* **Testing Framework:** Playwright (for End-to-End browser-driven automated testing)
* **App Type:** Single Page Application (SPA) serving an interactive `index.html` dashboard on Port `3000`.

---

## 🚀 Quick Start Instructions

### 1. Prerequisites
Before running or building, ensure you have the following tools installed locally:
* **Node.js:** LTS version (v18 or newer recommended)
* **npm:** Node Package Manager (comes bundled with Node.js)

### 2. Installing Dependencies
Install the necessary runtime dependency packages (`express` and `cors`) along with development dependencies (`@playwright/test`):
```bash
npm install
```
### 3. Running the Frontend Microservice
Start the Express server locally:

```bash
npm start
```
Once running, the microservice spins up on Port 3000 (default):

Local Web Interface: http://localhost:3000

### 4. 🧪 Testing with Playwright (E2E)
The codebase includes a fully-configured E2E integration test suite (tests/simulation.spec.js) that verifies the user interface behavior. It launches Playwright, intercepts API requests (mocking endpoints like POST /sessions and POST /sessions/{id}/simulate), simulates the HTML button click, validates the animation intervals (delay frames), and asserts that state labels update appropriately.

#### 4.1. Install Playwright Browsers
If running Playwright for the first time, install the required browser binaries (Chromium, Firefox, WebKit):

```bash
npx playwright install
```
#### 4.2. Run All E2E Tests
Execute the test suite locally. Playwright will automatically spin up server.js using the web server command configured in playwright.config.js, run the tests, and shut it down cleanly:

```bash
npx playwright test
```
#### 4.3. Debugging / UI Mode
To view the browser running live and step through each execution frame:

```bash
npx playwright test --ui
```
### 5.📁 Directory Layout
Your project directory should follow this structure to align with server.js and playwright.config.js:

```bash
Plaintext
game-frontend-service/
├── public/
│   └── index.html            <-- Main user interface and animation logic
├── tests/
│   └── simulation.spec.js    <-- Playwright E2E simulation test
├── package.json              <-- Node dependency configuration
├── playwright.config.js      <-- Playwright execution properties
├── server.js                 <-- Express static file host server
└── README.md                 <-- This instructions file
```
### 6.🔌 API Connections
The frontend application connects to the backend Game Session Service:

Target Base Endpoint: http://localhost:8081 (CORS must be enabled on the Session Service)

Main Workflow:

* Requests POST http://localhost:8081/sessions to initialize a session ID.

* Requests POST http://localhost:8081/sessions/{sessionId}/simulate to retrieve the randomly generated move history.

* Uses a progressive rendering technique (delay(800)) to display the simulated moves consecutively in real-time on the 3x3 board layout.
