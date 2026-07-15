package com.company.game_session_service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.company.game_session_service.entity.GameSessionEntity;
import com.company.game_session_service.model.StartSessionRequest;
import com.company.game_session_service.service.GameSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/sessions")
@Tag(name = "Game Session Coordinator", description = "Endpoints to create and automatically simulate gameplay sessions")
public class SessionController {

    private static final Logger log = LoggerFactory.getLogger(SessionController.class);

    private final GameSessionService sessionService;

    public SessionController(GameSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    @Operation(summary = "Create a game session", description = "Initializes a session in H2 and provisions game state inside the Engine Service.")
    public ResponseEntity<?> createSession(@RequestBody StartSessionRequest request) {
        try {
            GameSessionEntity session = sessionService.createSession(request.playerXId(), request.playerOId());
            return ResponseEntity.status(HttpStatus.CREATED).body(session);
        } catch (Exception e) {
            log.error("Failed to create game session.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{sessionId}/simulate")
    @Operation(summary = "Simulate complete gameplay", description = "Triggers loop generation making random moves for both players until a draw or win is achieved.")
    public ResponseEntity<?> simulateGame(@PathVariable("sessionId") String sessionId) {
        try {
            GameSessionEntity finishedSession = sessionService.simulateGame(sessionId);
            return ResponseEntity.ok(finishedSession);
        } catch (Exception e) {
            log.error("Failed to simulate the game.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get Session profile", description = "Returns active status, player mappings, and logged move-by-move histories.")
    public ResponseEntity<?> getSession(@PathVariable("sessionId") String sessionId) {
        try {
            GameSessionEntity session = sessionService.getSession(sessionId);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            log.error("Failed to get session with id={}", sessionId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
