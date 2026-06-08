package project.cheqmate.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "cheqmate.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaTopicConfig {

    public static final String NOTIFICATIONS = "cheqmate.notifications";
    public static final String NOTIFICATIONS_DLT = "cheqmate.notifications.DLT";

    private static final int PARTITIONS = 3;
    private static final short REPLICAS = 1;

    @Bean
    public NewTopic notificationsTopic() {
        return TopicBuilder.name(NOTIFICATIONS).partitions(PARTITIONS).replicas(REPLICAS).build();
    }

    @Bean
    public NewTopic notificationsDltTopic() {
        return TopicBuilder.name(NOTIFICATIONS_DLT).partitions(PARTITIONS).replicas(REPLICAS).build();
    }
}
