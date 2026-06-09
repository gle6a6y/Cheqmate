package project.cheqmate.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "cheqmate.kafka.enabled=true",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@EmbeddedKafka(partitions = 1, topics = {"cheqmate.notifications", "cheqmate.notifications.DLT"})
class NotificationKafkaNullGroupIdTest {

    @Autowired
    private NotificationDispatcher dispatcher;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockitoBean
    private SseNotificationService sseNotificationService;

    @MockitoBean
    private FcmService fcmService;

    @BeforeEach
    void clearNotifications() {
        notificationRepository.deleteAll();
    }

    @Test
    void messageWithNullGroupId_roundTripsIntactToBothConsumers() {
        NotificationMessage message =
                new NotificationMessage("carol", "GROUP_CREATED", "Новая группа", "Bob создал группу", null);

        dispatcher.dispatch(message);

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
                assertThat(notificationRepository.findByRecipientUsernameOrderByCreatedAtDesc("carol"))
                        .hasSize(1)
                        .first()
                        .satisfies(n -> {
                            assertThat(n.getType()).isEqualTo("GROUP_CREATED");
                            assertThat(n.getGroupId()).isNull();
                        }));

        ArgumentCaptor<NotificationMessage> captor = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(fcmService, timeout(20_000)).sendNotification(captor.capture());
        assertThat(captor.getValue().groupId()).isNull();
        assertThat(captor.getValue().recipientUsername()).isEqualTo("carol");
        assertThat(captor.getValue().type()).isEqualTo("GROUP_CREATED");
    }
}
