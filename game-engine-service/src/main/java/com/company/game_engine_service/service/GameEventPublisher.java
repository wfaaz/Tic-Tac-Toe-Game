package com.company.game_engine_service.service;

import com.company.game_engine_service.entity.TicTacToeGameEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class GameEventPublisher {

    // @Async executes this in a separate thread pool so it does not block the REST thread
    @Async
    public void publishGameOver(TicTacToeGameEntity game) {
        System.out.println("Asynchronously publishing Game Over Event for ID: " + game.getGameId());
        System.out.println("Outcome: " + game.getStatus() + " | Winner/Draw details are broadcasted to Kafka.");

        // Inside this method you would implement your KafkaTemplate.send()
        // or RabbitTemplate.convertAndSend() logic to notify Player/Stats service
    }
}
