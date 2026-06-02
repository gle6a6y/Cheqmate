package project.cheqmate.controller;

import org.springframework.web.bind.annotation.*;
import project.cheqmate.dto.FortuneWheelRequest;
import project.cheqmate.dto.FortuneWheelResponse;

import java.util.Random;

@RestController
@RequestMapping("/api/fortune-wheel")
public class FortuneWheelController {

    private final String[] participants = {"Катя", "Иван", "Олег", "Алина"};
    private final Random random = new Random();

    @PostMapping("/spin")
    public FortuneWheelResponse spinWheel(@RequestBody FortuneWheelRequest request) {
        // Простая реализация рулетки - случайный выбор из участников
        String loser = participants[random.nextInt(participants.length)];
        
        // Здесь можно добавить логику:
        // 1. Получение реальных участников группы из базы данных
        // 2. Учет балансов и истории платежей
        // 3. Более сложную логику выбора
        
        return new FortuneWheelResponse(loser);
    }
    
    @GetMapping("/test")
    public FortuneWheelResponse testSpin() {
        String loser = participants[random.nextInt(participants.length)];
        return new FortuneWheelResponse(loser);
    }
}