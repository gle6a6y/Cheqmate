package project.cheqmate.event;

import java.util.List;

public record ChequeAddedEvent(
        List<String> targetUsernames,
        String chequeName,
        String groupName,
        String creatorName
) {}