package com.company.game_engine_service.repository;

import com.company.game_engine_service.entity.TicTacToeGameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicTacToeRepository extends JpaRepository<TicTacToeGameEntity, String> {
    // Basic CRUD methods like findById, save, and delete are inherited automatically
}