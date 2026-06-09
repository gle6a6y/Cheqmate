package project.cheqmate.event;

public record NotificationMessage(
        String recipientUsername,
        String type,
        String title,
        String body,
        Integer groupId
) {}
