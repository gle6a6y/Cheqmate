package project.cheqmate.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_achievements", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "achievement_key"})
})
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "achievement_key", nullable = false)
    private String achievementKey;

    @Column(nullable = false)
    private LocalDateTime unlockedAt;

    public UserAchievement(User user, String achievementKey) {
        this.user = user;
        this.achievementKey = achievementKey;
        this.unlockedAt = LocalDateTime.now();
    }
}
