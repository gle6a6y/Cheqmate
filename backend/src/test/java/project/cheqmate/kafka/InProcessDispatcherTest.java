package project.cheqmate.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import project.cheqmate.event.NotificationMessage;
import project.cheqmate.listener.NotificationKafkaConsumer;
import project.cheqmate.service.FcmService;
import project.cheqmate.service.InProcessNotificationDispatcher;
import project.cheqmate.service.KafkaNotificationDispatcher;
import project.cheqmate.service.NotificationDispatcher;
import project.cheqmate.service.NotificationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = "cheqmate.kafka.enabled=false")
class InProcessDispatcherTest {

    @Autowired
    private NotificationDispatcher dispatcher;

    @Autowired
    private ApplicationContext context;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private FcmService fcmService;

    @Test
    void dispatch_synchronouslyDrivesPersistAndFcm() {
        NotificationMessage message =
                new NotificationMessage("alice", "CHEQUE_ADDED", "Новый чек", "Bob добавил чек", 42);

        dispatcher.dispatch(message);

        assertThat(dispatcher).isInstanceOf(InProcessNotificationDispatcher.class);
        verify(notificationService).persistAndPushSse(message);
        verify(fcmService).sendNotification(message);
    }

    @Test
    void kafkaBeans_areAbsentWhenDisabled() {
        assertThat(context.getBeansOfType(InProcessNotificationDispatcher.class)).hasSize(1);

        assertThat(context.getBeansOfType(KafkaNotificationDispatcher.class)).isEmpty();
        assertThat(context.getBeansOfType(NotificationKafkaConsumer.class)).isEmpty();
        assertThat(context.containsBean("kafkaErrorHandler")).isFalse();
    }
}
