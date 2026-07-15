package com.company.game_session_service.service;

import com.company.game_session_service.entity.GameSessionEntity;
import com.company.game_session_service.entity.MoveRecord;
import com.company.game_session_service.model.EngineGameResponse;
import com.company.game_session_service.model.EngineMoveRequest;
import com.company.game_session_service.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class GameSessionService {

    private static final Logger log = LoggerFactory.getLogger(GameSessionService.class);

    private final SessionRepository repository;
    private final RestClient restClient;
    private final Random random = new Random();

    // Point this to where your TicTacToe Game Engine runs
    private static final String ENGINE_URL = "http://localhost:8080/games";

    public GameSessionService(SessionRepository repository) {
        this.repository = repository;
        this.restClient = RestClient.builder()
            .baseUrl(ENGINE_URL)
            .build();
    }

    public GameSessionEntity createSession(String playerXId, String playerOId) {
        // 1. Initialize State in Game Engine Service
        try {
            String gameId = restClient.post()
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new StartSessionRequestHelper(playerXId, playerOId)) // passing custom session ID to engine
                .retrieve()
                .body(String.class);

        // 2. Save Session to local H2
        GameSessionEntity session = new GameSessionEntity(gameId, playerXId, playerOId);
        return repository.save(session);

        } catch (Exception e) {
            throw new GameSessionServiceException("Failed to initialize game state in Game Engine Service: ", e);
        }
    }

    public GameSessionEntity simulateGame(String sessionId) {
        GameSessionEntity session = repository.findById(sessionId)
            .orElseThrow(() -> new GameSessionServiceException("Session not found: " + sessionId));

        if ("FINISHED".equals(session.getStatus())) {
            throw new GameSessionServiceException("Simulation already finished for this session.");
        }

        session.setStatus("RUNNING");
        boolean gameOver = false;

        while (!gameOver) {
            // 1. Get Current Game State from the Engine
            EngineGameResponse engineState = fetchEngineState(sessionId);

            if (!"IN_PROGRESS".equals(engineState.status())) {
                session.setStatus("FINISHED");
                repository.save(session);
                break;
            }

            // 2. Generate automated random move based on empty spots
            EngineMoveRequest move = generateRandomMove(engineState);

            // 3. Post move to Game Engine
            EngineGameResponse updatedEngineState;
            try {
                updatedEngineState = restClient.post()
                    .uri("/{gameId}/move", sessionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(move)
                    .retrieve()
                    .body(EngineGameResponse.class);
            } catch (Exception e) {
                throw new GameSessionServiceException("Engine rejected automated move.", e);
            }

            // 4. Record Move History
            if (updatedEngineState != null) {
                String symbol = engineState.currentTurn(); // Turn symbol that just played
                MoveRecord moveRecord = new MoveRecord(move.playerId(), move.row(), move.col(), symbol);
                session.addMove(moveRecord);

                if (!"IN_PROGRESS".equals(updatedEngineState.status())) {
                    session.setStatus("FINISHED");
                    session.setGameResult(updatedEngineState.status());
                    gameOver = true;
                }
                repository.save(session);
            }
        }

        return session;
    }

    public GameSessionEntity getSession(String sessionId) {
        return repository.findById(sessionId)
            .orElseThrow(() -> new GameSessionServiceException("Session not found: " + sessionId));
    }

    private EngineGameResponse fetchEngineState(String sessionId) {
        return restClient.get()
            .uri("/{gameId}", sessionId)
            .retrieve()
            .body(EngineGameResponse.class);
    }

    private EngineMoveRequest generateRandomMove(EngineGameResponse state) {
        String activePlayerId = "X".equals(state.currentTurn()) ? state.playerXId() : state.playerOId();

        // Find all empty cells on the current board layout
        List<int[]> emptyCells = new ArrayList<>();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (state.board()[r][c] == null || state.board()[r][c].isEmpty()) {
                    emptyCells.add(new int[]{r, c});
                }
            }
        }

        if (emptyCells.isEmpty()) {
            throw new GameSessionServiceException("No available moves left on the board.");
        }

        // Pick one spot randomly
        int[] chosenCell = emptyCells.get(random.nextInt(emptyCells.size()));
        return new EngineMoveRequest(activePlayerId, chosenCell[0], chosenCell[1]);
    }

    // Helper static DTO to conform to Engine's creation interface
    private static record StartSessionRequestHelper(String playerXId, String playerOId) {}

    public static class GameSessionServiceException extends RuntimeException {
        public GameSessionServiceException(String message) {
            super(message);
        }

        public GameSessionServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
