package project.cheqmate.event;

public record UserAddedToGroupEvent(
        String targetUsername,
        String groupName,
        String inviterName
) {}