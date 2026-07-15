package com.company.game_engine_service.entity;

import com.company.game_engine_service.repository.BoardConverter;
import jakarta.persistence.*;
import java.util.Arrays;
import java.util.UUID;

@Entity
@Table(name = "games")
public class TicTacToeGameEntity {

    @Id
    @Column(name = "game_id")
    private String gameId;

    @Column(name = "player_x_id", nullable = false)
    private String playerXId;

    @Column(name = "player_o_id", nullable = false)
    private String playerOId;

    @Convert(converter = BoardConverter.class)
    @Column(name = "board", nullable = false, length = 100)
    private String[][] board;

    @Column(name = "current_turn", nullable = false)
    private String currentTurn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GameStatus status;

    public TicTacToeGameEntity() {
    }

    public TicTacToeGameEntity(String playerXId, String playerOId) {
        this.gameId = UUID.randomUUID().toString();
        this.playerXId = playerXId;
        this.playerOId = playerOId;
        this.currentTurn = "X";
        this.status = GameStatus.IN_PROGRESS;
        this.board = new String[3][3];
        for (String[] row : board) {
            Arrays.fill(row, "");
        }
    }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getPlayerXId() { return playerXId; }
    public void setPlayerXId(String playerXId) { this.playerXId = playerXId; }

    public String getPlayerOId() { return playerOId; }
    public void setPlayerOId(String playerOId) { this.playerOId = playerOId; }

    public String[][] getBoard() { return board; }
    public void setBoard(String[][] board) { this.board = board; }

    public String getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(String currentTurn) { this.currentTurn = currentTurn; }

    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }
}
