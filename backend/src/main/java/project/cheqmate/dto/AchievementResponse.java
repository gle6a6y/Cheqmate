package project.cheqmate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AchievementResponse {
    private String key;
    private String name;
    private String description;
    private LocalDateTime unlockedAt;
}
