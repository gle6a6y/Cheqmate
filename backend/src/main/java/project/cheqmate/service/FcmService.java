package project.cheqmate.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.cheqmate.model.DeviceToken;
import project.cheqmate.repository.DeviceTokenRepository;

import java.util.List;
import java.util.Map;


@Service
public class FcmService {

    private static final Logger log = LoggerFactory.getLogger(FcmService.class);

    private final DeviceTokenRepository deviceTokenRepository;

    public FcmService(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Transactional
    public void sendToUser(String username, String title, String body, Map<String, String> data) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.debug("FCM disabled (Firebase not initialized), skipping push to '{}'", username);
            return;
        }
        List<DeviceToken> tokens = deviceTokenRepository.findByUsername(username);
        if (tokens.isEmpty()) {
            log.debug("FCM: no device tokens registered for user '{}', skipping push", username);
            return;
        }
        for (DeviceToken deviceToken : tokens) {
            sendToToken(deviceToken, title, body, data);
        }
    }

    private void sendToToken(DeviceToken deviceToken, String title, String body, Map<String, String> data) {
        Message message = Message.builder()
                .setToken(deviceToken.getToken())
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .build();
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM sent to user '{}': {}", deviceToken.getUsername(), response);
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode code = e.getMessagingErrorCode();
            if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                log.info("FCM: removing stale token for user '{}' ({})", deviceToken.getUsername(), code);
                deviceTokenRepository.delete(deviceToken);
            } else {
                log.warn("FCM: send failed for user '{}': {}", deviceToken.getUsername(), e.getMessage());
            }
        }
    }
}
