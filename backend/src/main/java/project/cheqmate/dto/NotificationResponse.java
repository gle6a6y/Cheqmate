package project.cheqmate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.cheqmate.model.Notification;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private Integer id;
    private String type;
    private String title;
    private String body;
    private Integer groupId;
    private boolean read;
    private Instant createdAt;

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getBody(),
                n.getGroupId(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
