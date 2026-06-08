package project.cheqmate.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import project.cheqmate.event.NotificationMessage;
import project.cheqmate.service.FcmService;
import project.cheqmate.service.NotificationDispatcher;
import project.cheqmate.service.NotificationService;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "cheqmate.kafka.enabled=true",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@EmbeddedKafka(partitions = 1, topics = {"cheqmate.notifications", "cheqmate.notifications.DLT"})
class NotificationKafkaDltTest {

    private static final String DLT_TOPIC = "cheqmate.notifications.DLT";

    @Autowired
    private NotificationDispatcher dispatcher;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private FcmService fcmService;

    @Test
    void failingConsumer_routesRecordToDeadLetterTopic() {
        BDDMockito.given(notificationService.persistAndPushSse(any(NotificationMessage.class)))
                .willThrow(new RuntimeException("simulated persistence failure"));

        try (Consumer<String, String> dltConsumer = createDltConsumer()) {
            dltConsumer.subscribe(List.of(DLT_TOPIC));

            dispatcher.dispatch(new NotificationMessage("alice", "CHEQUE_ADDED", "t", "b", 7));

            ConsumerRecord<String, String> dltRecord =
                    KafkaTestUtils.getSingleRecord(dltConsumer, DLT_TOPIC, Duration.ofSeconds(30));

            assertThat(dltRecord.key()).isEqualTo("alice");
            assertThat(dltRecord.value()).contains("CHEQUE_ADDED");
        }

        verify(notificationService, times(4)).persistAndPushSse(any(NotificationMessage.class));


        verify(fcmService, timeout(5_000)).sendNotification(any(NotificationMessage.class));
    }

    private Consumer<String, String> createDltConsumer() {
        Map<String, Object> props = KafkaTestUtils.consumerProps("dlt-test-group", "true", embeddedKafka);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<String, String>(props).createConsumer();
    }
}
