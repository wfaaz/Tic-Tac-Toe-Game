package com.company.game_engine_service.model;

public class StartGameRequest {
    private String playerXId;
    private String playerOId;

    // Constructors, Getters, and Setters
    public StartGameRequest() {}

    public StartGameRequest(String playerXId, String playerOId) {
        this.playerXId = playerXId;
        this.playerOId = playerOId;
    }

    public String getPlayerXId() { return playerXId; }
    public void setPlayerXId(String playerXId) { this.playerXId = playerXId; }

    public String getPlayerOId() { return playerOId; }
    public void setPlayerOId(String playerOId) { this.playerOId = playerOId; }
}
