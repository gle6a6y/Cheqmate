package project.cheqmate.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import project.cheqmate.event.NotificationMessage;
import project.cheqmate.repository.NotificationRepository;
import project.cheqmate.service.FcmService;
import project.cheqmate.service.NotificationDispatcher;
import project.cheqmate.service.SseNotificationService;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "cheqmate.kafka.enabled=true",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@EmbeddedKafka(partitions = 1, topics = {"cheqmate.notifications", "cheqmate.notifications.DLT"})
class NotificationKafkaFlowTest {

    @Autowired
    private NotificationDispatcher dispatcher;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockitoBean
    private SseNotificationService sseNotificationService;

    @MockitoBean
    private FcmService fcmService;

    @Test
    void dispatchedNotification_isPersisted_andPushedViaSseAndFcm() {
        NotificationMessage message =
                new NotificationMessage("alice", "CHEQUE_ADDED", "Новый чек", "Bob добавил чек", 42);

        dispatcher.dispatch(message);

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
                assertThat(notificationRepository.findByRecipientUsernameOrderByCreatedAtDesc("alice"))
                        .hasSize(1)
                        .first()
                        .satisfies(n -> {
                            assertThat(n.getType()).isEqualTo("CHEQUE_ADDED");
                            assertThat(n.getGroupId()).isEqualTo(42);
                            assertThat(n.isRead()).isFalse();
                        }));

        verify(sseNotificationService, timeout(20_000)).sendNotification(eq("alice"), any());

        verify(fcmService, timeout(20_000)).sendNotification(any(NotificationMessage.class));
    }
}
