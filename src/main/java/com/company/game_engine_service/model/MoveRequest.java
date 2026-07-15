package com.company.game_engine_service.model;

public class MoveRequest {
    private String playerId;
    private int row; // 0, 1, or 2
    private int col; // 0, 1, or 2

    // Constructors, Getters, and Setters
    public MoveRequest() {}

    public String getPlayerId() { return playerId; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public void setRow(int row) { this.row = row; }
    public void setCol(int col) { this.col = col; }

    @Override
    public String toString() {
        return "MoveRequest{" +
            "playerId='" + playerId + '\'' +
            ", row=" + row +
            ", col=" + col +
            '}';
    }
}