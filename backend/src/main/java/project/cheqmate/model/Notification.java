package project.cheqmate.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String recipientUsername;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String body;

    private Integer groupId;

    @Column(nullable = false)
    private boolean read = false;

    @Column(nullable = false)
    private Instant createdAt;

    public Notification(String recipientUsername, String type, String title, String body, Integer groupId) {
        this.recipientUsername = recipientUsername;
        this.type = type;
        this.title = title;
        this.body = body;
        this.groupId = groupId;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
