package project.cheqmate.event;

public record AchievementUnlockedEvent(
        String username,
        String achievementKey,
        String achievementName
) {}
