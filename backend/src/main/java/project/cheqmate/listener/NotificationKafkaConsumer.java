package project.cheqmate.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import project.cheqmate.config.KafkaTopicConfig;
import project.cheqmate.event.NotificationMessage;
import project.cheqmate.service.FcmService;
import project.cheqmate.service.NotificationService;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cheqmate.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationKafkaConsumer {

    private final NotificationService notificationService;
    private final FcmService fcmService;

    @KafkaListener(topics = KafkaTopicConfig.NOTIFICATIONS, groupId = "notif-persist")
    public void persistAndPushSse(NotificationMessage message) {
        notificationService.persistAndPushSse(message);
    }

    @KafkaListener(topics = KafkaTopicConfig.NOTIFICATIONS, groupId = "notif-fcm")
    public void sendFcm(NotificationMessage message) {
        fcmService.sendNotification(message);
    }
}
