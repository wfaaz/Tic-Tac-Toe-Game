package com.company.game_session_service;

import com.company.game_session_service.model.EngineGameResponse;
import com.company.game_session_service.model.StartSessionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.company.game_session_service.entity.GameSessionEntity;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GameSessionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper objectMapper;


    private static WireMockServer wireMockServer;
    private static final String SESSION_ID = "session-123-abc";
    private static final String PLAYER_X = "alice";
    private static final String PLAYER_O = "bob";

    @BeforeAll
    static void startWireMock() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // Optional: Prevents failure if dates are serialized as numeric timestamps
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Spin up Wiremock on port 8080 where GameSessionService expects the Engine to live
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8080);
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }

    @Test
    @DisplayName("Simulate full automated gameplay flow via REST APIs and verify final persistent states")
    void testFullAutomatedSessionAndGameplay() throws Exception {

        // ==========================================
        // 1. STUB ENGINE RESPONSES (WIREMOCK)
        // Set up the exact sequential states for a 3-move Game Win
        // ==========================================

        // POST /games -> Create game and return session ID
        stubFor(WireMock.post(urlEqualTo("/games"))
            .willReturn(aResponse()
                .withStatus(201)
                .withBody(SESSION_ID)));

        // Define state boards
        String[][] emptyBoard = {{"", "", ""}, {"", "", ""}, {"", "", ""}};
        String[][] turn1Board = {{"X", "", ""}, {"", "", ""}, {"", "", ""}};
        String[][] turn2Board = {{"X", "", ""}, {"O", "", ""}, {"", "", ""}};
        String[][] winningBoard = {{"X", "X", "X"}, {"O", "", ""}, {"", "", ""}};

        // Sequence of GET calls mapping state loops
        // Loop Iteration 1: Fetch state (In Progress, X's Turn)
        stubFor(WireMock.get(urlEqualTo("/games/" + SESSION_ID))
            .inScenario("Gameplay")
            .whenScenarioStateIs(com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED)
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(new EngineGameResponse(SESSION_ID, PLAYER_X, PLAYER_O, emptyBoard, "X", "IN_PROGRESS"))))
            .willSetStateTo("Move 1 Played"));

        // Loop Iteration 1: POST Move 1 (Alice plays X at any spot)
        stubFor(WireMock.post(urlEqualTo("/games/" + SESSION_ID + "/move"))
            .inScenario("Gameplay")
            .whenScenarioStateIs("Move 1 Played")
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(new EngineGameResponse(SESSION_ID, PLAYER_X, PLAYER_O, turn1Board, "O", "IN_PROGRESS")))));

        // Loop Iteration 2: Fetch state (In Progress, O's Turn)
        stubFor(WireMock.get(urlEqualTo("/games/" + SESSION_ID))
            .inScenario("Gameplay")
            .whenScenarioStateIs("Move 1 Played")
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(new EngineGameResponse(SESSION_ID, PLAYER_X, PLAYER_O, turn1Board, "O", "IN_PROGRESS"))))
            .willSetStateTo("Move 2 Played"));

        // Loop Iteration 2: POST Move 2 (Bob plays O)
        stubFor(WireMock.post(urlEqualTo("/games/" + SESSION_ID + "/move"))
            .inScenario("Gameplay")
            .whenScenarioStateIs("Move 2 Played")
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(new EngineGameResponse(SESSION_ID, PLAYER_X, PLAYER_O, turn2Board, "X", "IN_PROGRESS")))));

        // Loop Iteration 3: Fetch State (In Progress, X's Turn)
        stubFor(WireMock.get(urlEqualTo("/games/" + SESSION_ID))
            .inScenario("Gameplay")
            .whenScenarioStateIs("Move 2 Played")
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(new EngineGameResponse(SESSION_ID, PLAYER_X, PLAYER_O, turn2Board, "X", "IN_PROGRESS"))))
            .willSetStateTo("Move 3 Played"));

        // Loop Iteration 3: POST Move 3 (Alice plays X for the Row Win!)
        stubFor(WireMock.post(urlEqualTo("/games/" + SESSION_ID + "/move"))
            .inScenario("Gameplay")
            .whenScenarioStateIs("Move 3 Played")
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(new EngineGameResponse(SESSION_ID, PLAYER_X, PLAYER_O, winningBoard, "O", "X_WON")))));

        // ==========================================
        // 2. RUN INTEGRATION TEST STEPS
        // ==========================================

        // Step A: Create Session via REST Endpoint
        StartSessionRequest startRequest = new StartSessionRequest(PLAYER_X, PLAYER_O);
        MvcResult sessionResult = mockMvc.perform(post("/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(startRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        GameSessionEntity session = objectMapper.readValue(
            sessionResult.getResponse().getContentAsString(),
            GameSessionEntity.class
        );

        assertEquals(SESSION_ID, session.getSessionId());
        assertEquals("CREATED", session.getStatus());

        // Step B: Trigger the game simulation
        MvcResult simulationResult = mockMvc.perform(post("/sessions/" + SESSION_ID + "/simulate"))
            .andExpect(status().isOk())
            .andReturn();

        GameSessionEntity simulatedSession = objectMapper.readValue(
            simulationResult.getResponse().getContentAsString(),
            GameSessionEntity.class
        );

        // Step C: Verify outcomes and game persistent history in H2
        assertEquals("FINISHED", simulatedSession.getStatus());
        assertEquals("X_WON", simulatedSession.getGameResult());
        assertEquals(3, simulatedSession.getMoveHistory().size()); // 3 moves were recorded and persisted

        // Step D: Validate final persistence via Get Endpoint
        MvcResult fetchResult = mockMvc.perform(get("/sessions/" + SESSION_ID))
            .andExpect(status().isOk())
            .andReturn();

        GameSessionEntity fetchedSession = objectMapper.readValue(
            fetchResult.getResponse().getContentAsString(),
            GameSessionEntity.class
        );

        assertEquals("FINISHED", fetchedSession.getStatus());
        assertEquals("X_WON", fetchedSession.getGameResult());
    }
}