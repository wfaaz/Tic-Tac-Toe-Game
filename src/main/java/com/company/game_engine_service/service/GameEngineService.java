package com.company.game_engine_service.service;

import com.company.game_engine_service.entity.TicTacToeGameEntity;
import com.company.game_engine_service.entity.GameStatus;
import com.company.game_engine_service.model.MoveRequest;
import com.company.game_engine_service.repository.TicTacToeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class GameEngineService {

    private final TicTacToeRepository repository;
    private final GameEventPublisher eventPublisher;

    public GameEngineService(TicTacToeRepository repository, GameEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new Game and returns it with game UUID.
     * */
    public TicTacToeGameEntity startNewGame(String playerXId, String playerOId) {
        TicTacToeGameEntity newGame = new TicTacToeGameEntity(playerXId, playerOId);
        return repository.save(newGame);
    }

    /**
     * Loads the game state from H2, validates the move, updates the state, 
     * saves it back to H2, and triggers an event if the game ended.
     */
    @Transactional
    public TicTacToeGameEntity makeMove(String gameId, MoveRequest move) {
        // 1. Fetch current state from H2 Database
        TicTacToeGameEntity game = repository.findById(gameId)
            .orElseThrow(() -> new GameEngineServiceException("Game not found with ID: " + gameId));

        // 2. Validate and execute the logic
        processMove(game, move);

        // 3. Save the updated state back to H2 Database
        TicTacToeGameEntity savedGame = repository.save(game);

        // 4. If game is over, publish an event asynchronously (Kafka/RabbitMQ style)
        if (savedGame.getStatus() != GameStatus.IN_PROGRESS) {
            eventPublisher.publishGameOver(savedGame);
        }

        return savedGame;
    }

    public void processMove(TicTacToeGameEntity game, MoveRequest move) {
        // 1. Rule Validation
        validateMove(game, move);

        // 2. Apply Move
        String symbol = game.getCurrentTurn();
        game.getBoard()[move.getRow()][move.getCol()] = symbol;

        // 3. Check for Game Over Conditions
        if (checkWin(game.getBoard(), symbol)) {
            game.setStatus(symbol.equals("X") ? GameStatus.X_WON : GameStatus.O_WON);
        } else if (isBoardFull(game.getBoard())) {
            game.setStatus(GameStatus.DRAW);
        } else {
            // Switch turns if game continues
            game.setCurrentTurn(symbol.equals("X") ? "O" : "X");
        }
    }

    public TicTacToeGameEntity getGame(String gameId) {
        return repository.findById(gameId)
            .orElseThrow(() -> new GameEngineServiceException("Game not found with ID: " + gameId));
    }

    private void validateMove(TicTacToeGameEntity game, MoveRequest move) {
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new GameEngineServiceException("Game has already finished.");
        }

        // Verify it is actually this player's turn
        String expectedPlayerId = game.getCurrentTurn().equals("X") ? game.getPlayerXId() : game.getPlayerOId();
        if (!Objects.equals(expectedPlayerId, move.getPlayerId())) {
            throw new GameEngineServiceException("It is not your turn.");
        }

        // Coordinate boundaries
        if (move.getRow() < 0 || move.getRow() > 2 || move.getCol() < 0 || move.getCol() > 2) {
            throw new GameEngineServiceException("Move is out of board boundaries.");
        }

        // Cell availability
        if (!game.getBoard()[move.getRow()][move.getCol()].isEmpty()) {
            throw new GameEngineServiceException("Cell is already occupied.");
        }
    }

    private boolean checkWin(String[][] board, String s) {
        // Check Rows and Columns
        for (int i = 0; i < 3; i++) {
            if (board[i][0].equals(s) && board[i][1].equals(s) && board[i][2].equals(s)) return true;
            if (board[0][i].equals(s) && board[1][i].equals(s) && board[2][i].equals(s)) return true;
        }
        // Check Diagonals
        if (board[0][0].equals(s) && board[1][1].equals(s) && board[2][2].equals(s)) return true;
        if (board[0][2].equals(s) && board[1][1].equals(s) && board[2][0].equals(s)) return true;

        return false;
    }

    private boolean isBoardFull(String[][] board) {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c].isEmpty()) return false;
            }
        }
        return true;
    }
    
    public static class GameEngineServiceException extends RuntimeException {
        public GameEngineServiceException(String message) {
            super(message);
        }

        public GameEngineServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}