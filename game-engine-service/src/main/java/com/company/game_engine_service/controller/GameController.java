package com.company.game_engine_service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.company.game_engine_service.entity.TicTacToeGameEntity;
import com.company.game_engine_service.model.MoveRequest;
import com.company.game_engine_service.model.StartGameRequest;
import com.company.game_engine_service.service.GameEngineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games")
public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    private final GameEngineService gameEngine;

    public GameController(GameEngineService gameEngine) {
        this.gameEngine = gameEngine;
    }

    @PostMapping
//    @Operation(
//        summary = "Start a new game",
//        description = "Creates a fresh Tic-Tac-Toe game record in the H2 database with an empty 3x3 board."
//    )
    public ResponseEntity<?> startGame(@RequestBody StartGameRequest request) {
        try {
            TicTacToeGameEntity newGame = gameEngine.startNewGame(request.getPlayerXId(), request.getPlayerOId());
            return ResponseEntity.status(201).body(newGame.getGameId()); // 201 Created
        } catch (Exception e) {
            log.error("Failed to save a new game. ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * GET /games/{gameId}
     * Retrieves the current state of the board, current turn, and game status.
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<?> getGame(@PathVariable("gameId") String gameId) {
        try {
            TicTacToeGameEntity game = gameEngine.getGame(gameId);
            return ResponseEntity.ok(game);
        } catch (IllegalArgumentException e) {
            log.error("Failed to get game with Id={}", gameId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * POST /games/{gameId}/move
     * Validates and applies a player's move.
     */
    @PostMapping("/{gameId}/move")
    public ResponseEntity<?> makeMove(
        @PathVariable("gameId") String gameId,
        @RequestBody MoveRequest moveRequest) {
        try {
            TicTacToeGameEntity updatedGame = gameEngine.makeMove(gameId, moveRequest);
            return ResponseEntity.ok(updatedGame);
        } catch (IllegalArgumentException e) {
            log.error("Failed to do a move with Id={}, request:={}", gameId, moveRequest, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
