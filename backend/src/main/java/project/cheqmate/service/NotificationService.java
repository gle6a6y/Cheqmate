package project.cheqmate.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.cheqmate.dto.NotificationResponse;
import project.cheqmate.model.DeviceToken;
import project.cheqmate.model.Notification;
import project.cheqmate.repository.DeviceTokenRepository;
import project.cheqmate.repository.NotificationRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final SseNotificationService sseNotificationService;
    private final FcmService fcmService;

    public NotificationService(NotificationRepository notificationRepository,
                               DeviceTokenRepository deviceTokenRepository,
                               SseNotificationService sseNotificationService,
                               FcmService fcmService) {
        this.notificationRepository = notificationRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.sseNotificationService = sseNotificationService;
        this.fcmService = fcmService;
    }

    @Transactional
    public Notification notify(String recipientUsername, String type, String title, String body, Integer groupId) {
        Notification saved = notificationRepository.save(
                new Notification(recipientUsername, type, title, body, groupId));

        NotificationResponse payload = NotificationResponse.from(saved);

        sseNotificationService.sendNotification(recipientUsername, payload);

        Map<String, String> data = new HashMap<>();
        data.put("notificationId", String.valueOf(saved.getId()));
        data.put("type", type);
        if (groupId != null) {
            data.put("groupId", String.valueOf(groupId));
        }
        fcmService.sendToUser(recipientUsername, title, body, data);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getHistory(String username) {
        return notificationRepository.findByRecipientUsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String username) {
        return notificationRepository.countByRecipientUsernameAndReadFalse(username);
    }

    @Transactional
    public void markAsRead(String username, Integer notificationId) {
        Notification notification = notificationRepository
                .findByIdAndRecipientUsername(notificationId, username)
                .orElseThrow(() -> new NoSuchElementException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }


    @Transactional
    public void registerDevice(String username, String token, String platform) {
        deviceTokenRepository.findByToken(token).ifPresentOrElse(
                existing -> {
                    existing.setUsername(username);
                    existing.setPlatform(platform);
                    deviceTokenRepository.save(existing);
                },
                () -> deviceTokenRepository.save(new DeviceToken(username, token, platform))
        );
    }

    @Transactional
    public void unregisterDevice(String token) {
        deviceTokenRepository.deleteByToken(token);
    }
}
