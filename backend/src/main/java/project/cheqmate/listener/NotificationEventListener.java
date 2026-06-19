package project.cheqmate.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import project.cheqmate.event.AchievementUnlockedEvent;
import project.cheqmate.event.ChequeAddedEvent;
import project.cheqmate.event.DebtAddedEvent;
import project.cheqmate.event.NotificationMessage;
import project.cheqmate.event.UserAddedToGroupEvent;
import project.cheqmate.service.NotificationDispatcher;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationDispatcher notificationDispatcher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserAddedToGroup(UserAddedToGroupEvent event) {
        String title = "Новая группа";
        String body = String.format("%s добавил Вас в группу \"%s\"",
                event.inviterName(), event.groupName());

        notificationDispatcher.dispatch(new NotificationMessage(
                event.targetUsername(), "GROUP_INVITE", title, body, null));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChequeAdded(ChequeAddedEvent event) {
        String title = "Новый чек";
        String body = String.format("%s добавил чек \"%s\" в группу \"%s\"",
                event.creatorName(), event.chequeName(), event.groupName());

        for (String username : event.targetUsernames()) {
            if (!username.equals(event.creatorName())) {
                notificationDispatcher.dispatch(new NotificationMessage(
                        username, "CHEQUE_ADDED", title, body, null));
            }
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDebtAdded(DebtAddedEvent event) {
        String title = "Новый долг";
        String body = String.format("%s заплатил за Вас. Вы должны %s ₽ в группе \"%s\"",
                event.creditorName(), formatAmount(event.amount()), event.groupName());

        notificationDispatcher.dispatch(new NotificationMessage(
                event.debtorUsername(), "DEBT_ADDED", title, body, event.groupId()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAchievementUnlocked(AchievementUnlockedEvent event) {
        String title = "Новое достижение";
        String body = String.format("Вы получили достижение «%s»!", event.achievementName());

        notificationDispatcher.dispatch(new NotificationMessage(
                event.username(), "ACHIEVEMENT_UNLOCKED", title, body, null));
    }

    private static String formatAmount(double amount) {
        if (amount == Math.rint(amount)) {
            return String.format("%.0f", amount);
        }
        return String.format("%.2f", amount);
    }
}
