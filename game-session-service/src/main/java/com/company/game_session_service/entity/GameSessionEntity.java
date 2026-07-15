package com.company.game_session_service.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game_sessions")
public class GameSessionEntity {

    @Id
    private String sessionId; // Same as gameId in the Engine

    private String playerXId;
    private String playerOId;
    private String status; // CREATED, RUNNING, FINISHED
    private String gameResult;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id")
    private List<MoveRecord> moveHistory = new ArrayList<>();

    public GameSessionEntity() {}

    public GameSessionEntity(String sessionId, String playerXId, String playerOId) {
        this.sessionId = sessionId;
        this.playerXId = playerXId;
        this.playerOId = playerOId;
        this.status = "CREATED";
        this.gameResult = "IN_PROGRESS";
    }

    // Getters, Setters, and Helpers
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getPlayerXId() { return playerXId; }
    public void setPlayerXId(String playerXId) { this.playerXId = playerXId; }

    public String getPlayerOId() { return playerOId; }
    public void setPlayerOId(String playerOId) { this.playerOId = playerOId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<MoveRecord> getMoveHistory() { return moveHistory; }
    public void addMove(MoveRecord move) { this.moveHistory.add(move); }

    public String getGameResult() { return gameResult; }
    public void setGameResult(String gameResult) { this.gameResult = gameResult; }
}
