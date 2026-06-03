package project.cheqmate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import project.cheqmate.dto.NotificationResponse;
import project.cheqmate.dto.RegisterDeviceRequest;
import project.cheqmate.service.NotificationService;
import project.cheqmate.service.SseNotificationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SseNotificationService sseNotificationService;
    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal String username) {
        return sseNotificationService.createConnection(username);
    }

    @GetMapping
    public List<NotificationResponse> history(@AuthenticationPrincipal String username) {
        return notificationService.getHistory(username);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal String username) {
        return Map.of("count", notificationService.getUnreadCount(username));
    }

    @PostMapping("/{id}/read")
    public void markAsRead(@AuthenticationPrincipal String username, @PathVariable Integer id) {
        notificationService.markAsRead(username, id);
    }

    @PostMapping("/devices")
    public void registerDevice(@AuthenticationPrincipal String username,
                               @RequestBody RegisterDeviceRequest request) {
        notificationService.registerDevice(username, request.getToken(), request.getPlatform());
    }

    @DeleteMapping("/devices")
    public void unregisterDevice(@RequestParam String token) {
        notificationService.unregisterDevice(token);
    }
}
