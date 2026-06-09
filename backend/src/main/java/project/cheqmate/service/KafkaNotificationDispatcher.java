package project.cheqmate.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import project.cheqmate.config.KafkaTopicConfig;
import project.cheqmate.event.NotificationMessage;

@Component
@ConditionalOnProperty(name = "cheqmate.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaNotificationDispatcher implements NotificationDispatcher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaNotificationDispatcher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void dispatch(NotificationMessage message) {
        kafkaTemplate.send(KafkaTopicConfig.NOTIFICATIONS, message.recipientUsername(), message);
    }
}
