package com.company.game_session_service.model;

// DTO representing the response back from the Engine
public record EngineGameResponse(
    String gameId,
    String playerXId,
    String playerOId,
    String[][] board,
    String currentTurn,
    String status
) {}
