package com.company.game_session_service.repository;

import com.company.game_session_service.entity.GameSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<GameSessionEntity, String> {
}
