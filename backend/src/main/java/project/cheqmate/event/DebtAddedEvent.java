package project.cheqmate.event;

public record DebtAddedEvent(
        String debtorUsername,
        String creditorName,
        double amount,
        String groupName,
        Integer groupId
) {}
