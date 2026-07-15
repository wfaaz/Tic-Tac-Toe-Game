package com.company.game_engine_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.company.game_engine_service.entity.GameStatus;
import com.company.game_engine_service.entity.TicTacToeGameEntity;
import com.company.game_engine_service.model.MoveRequest;
import com.company.game_engine_service.model.StartGameRequest;
import com.company.game_engine_service.service.GameEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GamePlayIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GameEventPublisher eventPublisher;

    @Test
    @DisplayName("Simulate complete Tic-Tac-Toe game flow where Player X achieves a row win")
    public void testFullGameFlow_PlayerXWins() throws Exception {
        String playerX = "player-alice";
        String playerO = "player-bob";

        // ==========================================
        // STEP 1: CREATE A NEW GAME SESSION
        // ==========================================
        StartGameRequest startRequest = new StartGameRequest();
        startRequest.setPlayerXId(playerX);
        startRequest.setPlayerOId(playerO);

        MvcResult createResult = mockMvc.perform(post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(startRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        String gameId = createResult.getResponse().getContentAsString();
        assertNotNull(gameId);
        assertFalse(gameId.trim().isEmpty());

        // Verify game initial state in H2 database
        MvcResult initialGetResult = mockMvc.perform(get("/games/" + gameId))
            .andExpect(status().isOk())
            .andReturn();

        TicTacToeGameEntity gameState = objectMapper.readValue(
            initialGetResult.getResponse().getContentAsString(),
            TicTacToeGameEntity.class
        );
        assertEquals(GameStatus.IN_PROGRESS, gameState.getStatus());
        assertEquals("X", gameState.getCurrentTurn());

        // ==========================================
        // STEP 2: SIMULATE THE MOVES FLOW
        // Target Board Layout for X Win on Top Row:
        // [ X | X | X ]
        // [ O | O |   ]
        // [   |   |   ]
        // ==========================================

        // Move 1: Player X (Alice) plays at (0, 0)
        gameState = executeMove(gameId, playerX, 0, 0);
        assertEquals("O", gameState.getCurrentTurn());
        assertEquals("X", gameState.getBoard()[0][0]);

        // Move 2: Player O (Bob) plays at (1, 0)
        gameState = executeMove(gameId, playerO, 1, 0);
        assertEquals("X", gameState.getCurrentTurn());
        assertEquals("O", gameState.getBoard()[1][0]);

        // Move 3: Player X (Alice) plays at (0, 1)
        gameState = executeMove(gameId, playerX, 0, 1);
        assertEquals("O", gameState.getCurrentTurn());
        assertEquals("X", gameState.getBoard()[0][1]);

        // Move 4: Player O (Bob) plays at (1, 1)
        gameState = executeMove(gameId, playerO, 1, 1);
        assertEquals("X", gameState.getCurrentTurn());
        assertEquals("O", gameState.getBoard()[1][1]);

        // ==========================================
        // STEP 3: PLAY THE DECISIVE WINNING MOVE
        // ==========================================
        // Move 5: Player X (Alice) plays at (0, 2) to secure a row win
        gameState = executeMove(gameId, playerX, 0, 2);

        // ==========================================
        // STEP 4: VERIFY OUTCOME & ASYNC PUBLISHING
        // ==========================================
        assertEquals(GameStatus.X_WON, gameState.getStatus());
        assertEquals("X", gameState.getBoard()[0][2]);

        // Verify that the game event publisher emitted the game-over payload exactly once
        verify(eventPublisher, times(1)).publishGameOver(any(TicTacToeGameEntity.class));
    }

    /**
     * Helper utility to keep test flow highly readable
     */
    private TicTacToeGameEntity executeMove(String gameId, String playerId, int row, int col) throws Exception {
        MoveRequest moveRequest = new MoveRequest();
        moveRequest.setPlayerId(playerId);
        moveRequest.setRow(row);
        moveRequest.setCol(col);

        MvcResult result = mockMvc.perform(post("/games/" + gameId + "/move")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(moveRequest)))
            .andExpect(status().isOk())
            .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), TicTacToeGameEntity.class);
    }
}
