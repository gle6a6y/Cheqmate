package project.cheqmate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.cheqmate.model.User;
import project.cheqmate.model.UserAchievement;

import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Integer> {
    List<UserAchievement> findByUser(User user);
    boolean existsByUserAndAchievementKey(User user, String achievementKey);
}
