package project.cheqmate.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import project.cheqmate.event.NotificationMessage;

@Component
@ConditionalOnProperty(name = "cheqmate.kafka.enabled", havingValue = "false")
public class InProcessNotificationDispatcher implements NotificationDispatcher {

    private final NotificationService notificationService;
    private final FcmService fcmService;

    public InProcessNotificationDispatcher(NotificationService notificationService, FcmService fcmService) {
        this.notificationService = notificationService;
        this.fcmService = fcmService;
    }

    @Override
    public void dispatch(NotificationMessage message) {
        notificationService.persistAndPushSse(message);
        fcmService.sendNotification(message);
    }
}
