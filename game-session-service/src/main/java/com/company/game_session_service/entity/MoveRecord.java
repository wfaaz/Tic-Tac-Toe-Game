package com.company.game_session_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "move_history")
public class MoveRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerId;
    private int boardRow;
    private int boardCol;
    private String symbol;
    private LocalDateTime timestamp;

    public MoveRecord() {}

    public MoveRecord(String playerId, int boardRow, int boardCol, String symbol) {
        this.playerId = playerId;
        this.boardRow = boardRow;
        this.boardCol = boardCol;
        this.symbol = symbol;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getPlayerId() { return playerId; }
    public int getBoardRow() { return boardRow; }
    public int getBoardCol() { return boardCol; }
    public String getSymbol() { return symbol; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
